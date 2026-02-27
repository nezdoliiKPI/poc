package dev.nez.simulation.client;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import dev.nez.simulation.DeviceDataProducer;
import dev.nez.simulation.dto.LoginRequest;
import dev.nez.simulation.dto.RegisterRequest;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ApplicationScoped
public class ProducerClient {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @RestClient
    AuthRestClient authClient;

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    public void startSimulation(DeviceDataProducer producer) {
        registerDevice(new RegisterRequest(producer.device.hardwareId(),
                                           producer.device.password(),
                                           producer.device.topic()))
            .chain(() -> authClient.login(new LoginRequest(producer.device.hardwareId(), producer.device.password())))
            .subscribe().with(
                loginResponse -> {
                    producer.deviceId = loginResponse.deviceId();
                    producer.token = loginResponse.token();

                    startDeviceConnection(producer);
                },
                throwable -> Log.error("Failed to initialize device simulation", throwable)
            );
    }

    private Uni<Void> registerDevice(RegisterRequest request) {
        return authClient.register(request)
            .onFailure(WebApplicationException.class).recoverWithItem(ex -> {
                    if (ex.getResponse().getStatus() == 409) {
                    return null;
                }
                throw new RuntimeException("Registration failed: " + ex.getMessage());
            }).replaceWithVoid();
    }

    private void startDeviceConnection(DeviceDataProducer producer) {
        Mqtt5AsyncClient client = MqttClient.builder()
                .useMqttVersion5()
                .identifier(producer.device.hardwareId())
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .automaticReconnectWithDefaultConfig()
                .simpleAuth()
                    .username(producer.deviceId.toString())
                    .password(producer.token.getBytes())
                .applySimpleAuth()
                .buildAsync();

        client.connect().whenComplete((_, throwable) -> {
            if (throwable != null) {
                Log.error("Connection error: " + brokerHost, throwable);
                return;
            }
            Log.info("Connected to MQTT broker: " + brokerHost + ":" + brokerPort);

            scheduler.scheduleAtFixedRate(() -> {
                client
                    .publishWith()
                    .topic(producer.device.topic())
                    .payload(Json.encodeToBuffer(producer.getData()).getBytes())
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .messageExpiryInterval(10)
                    .send()
                    .whenComplete((_, pubThrowable) -> {
                        if (pubThrowable != null) {
                            Log.error("Send error", pubThrowable);
                        } else {
                            Log.info("Published: " + producer.getData());
                        }
                    });
            }, producer.initialDelay, producer.period, producer.unit);
        });
    }
}
