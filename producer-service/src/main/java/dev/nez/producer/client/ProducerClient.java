package dev.nez.producer.client;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import dev.nez.producer.simulation.generator.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.DeviceDataGenerator.MessageType;

import dev.nez.producer.dto.rest.LoginRequest;
import dev.nez.producer.dto.rest.RegisterRequest;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.producer.simulation.model.MessageTiming;
import dev.nez.producer.security.MqttTrustManagerProvider;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.Json;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@ApplicationScoped
public class ProducerClient {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    @RestClient
    AuthRestClient authClient;

    @Inject
    MqttTrustManagerProvider provider;

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    public void startSimulation(DeviceDataGenerator producer) {
        final var registerRequest = new RegisterRequest(
            producer.device.hardwareId(),
            producer.device.password(),
            producer.device.topic(),
            producer.device.batteryTopic(),
            producer.messageType
        );

        final var loginRequest = new LoginRequest(
            producer.device.hardwareId(),
            producer.device.password()
        );

        registerDevice(registerRequest)
                .chain(() -> authClient.login(loginRequest))
                .subscribe().with(loginResponse -> {
                        producer.deviceId = loginResponse.deviceId();
                        producer.token = loginResponse.token();
                        startDeviceConnection(producer);
                    },throwable -> Log.error("Failed to initialize device simulation", throwable)
                );
    }

    private Uni<Void> registerDevice(RegisterRequest request) {
        return authClient.register(request)
            .onFailure(WebApplicationException.class).recoverWithItem(Unchecked.function(ex -> {
                    if (ex.getResponse().getStatus() == 409) {
                    return null;
                }
                throw new RuntimeException("Registration failed: " + ex.getMessage());
            })).replaceWithVoid();
    }

    private void startDeviceConnection(DeviceDataGenerator producer) {
        final Mqtt5AsyncClient client = MqttClient.builder()
                .useMqttVersion5()
                .identifier(producer.device.hardwareId())
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .sslConfig()
                    .trustManagerFactory(provider.provide())
                .applySslConfig()
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
            Log.debug("Connected to MQTT broker: " + brokerHost + ":" + brokerPort);

            final var topic = producer.device.topic();
            final var mainTiming = producer.mainTiming;

            startSendingMessages(
                    client, producer.messageType, producer::getData, topic, mainTiming);

            if (!producer.batteryIsPresent()) {
                return;
            }

            final var batteryTopic = producer.device.batteryTopic();
            final var batteryTiming = producer.batteryTiming;

            startSendingMessages(
                    client, producer.messageType, producer::getBatteryData, batteryTopic, batteryTiming);
        });
    }

    private byte[] serialize(Object payload,  MessageType messageType) {
        return switch (messageType) {
            case JSON -> Json.encodeToBuffer(payload).getBytes();
            case PROTO -> {
                if (payload instanceof ProtocolBuffer b) {
                    yield b.serialize();
                } else {
                    throw new RuntimeException("Unexpected data type: " + payload.getClass());
                }
            }
        };
    }

    private void startSendingMessages(
        Mqtt5AsyncClient client,
        MessageType messageType,
        Supplier<Object> dataProvider,
        String topic,
        MessageTiming timing
    ) {
        scheduler.scheduleAtFixedRate(() -> {
            final var data = dataProvider.get();

            client
                .publishWith()
                .topic(topic)
                .payload(serialize(data, messageType))
                .qos(MqttQos.AT_LEAST_ONCE)
                .messageExpiryInterval(timing.messageTtlSeconds())
                .send()
                .whenComplete((_, pubThrowable) -> {
                    if (pubThrowable != null) {
                        Log.error("Send error", pubThrowable);
                    } else {
                        Log.debug("Published: " + data);
                    }
                });
        }, timing.initialDelay(), timing.period(), timing.unit());
    }
}
