package dev.nez.simulation;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Random;

@ApplicationScoped
public class ProducerService {

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    void onStart(@Observes StartupEvent ev) {
        Log.info("Start device simulation...");
        startDeviceConnection();
    }

    private void startDeviceConnection() {
        final Random random = new Random();
        final Long deviceId = 1L;

        Mqtt5AsyncClient client = MqttClient.builder()
                .useMqttVersion5()
                .identifier(deviceId.toString())
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .automaticReconnectWithDefaultConfig() // Вбудований надійний механізм перепідключення
                .buildAsync();

        client.connect().whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                Log.error("Connection error: " + brokerHost, throwable);
                return;
            }

            vertx.setPeriodic(500, timerId -> {
                Temperature payloadData = new Temperature(
                        deviceId,
                        20.0 + random.nextDouble() * 10.0
                );

                client
                    .publishWith()
                    .topic("dev/temp/j")
                    .payload(Json.encodeToBuffer(payloadData).getBytes())
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .messageExpiryInterval(10)
                    .send()
                    .whenComplete((publishResult, pubThrowable) -> {
                        if (pubThrowable != null) {
                            Log.error("Send error", pubThrowable);
                        } else {
                            Log.info("Published: " + payloadData);
                        }
                    });
            });
        });
    }
}
