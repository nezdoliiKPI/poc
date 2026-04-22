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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@ApplicationScoped
public class ProducerClient {
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    @RestClient
    AuthRestClient authClient;

    @Inject
    MqttTrustManagerProvider provider;

    public DeviceSession createSession(DeviceDataGenerator generator) {
        return new DeviceSession(generator);
    }

    public class DeviceSession {
        private final DeviceDataGenerator producer;
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private boolean isRegistered = false;

        private Mqtt5AsyncClient mqttClient;
        private final List<ScheduledFuture<?>> tasks = new ArrayList<>();

        private DeviceSession(DeviceDataGenerator producer) {
            this.producer = producer;
        }

        public float getIntensityPerSecond() {
            return producer.getIntensityPerSecond();
        }

        public boolean isRunning() {
            return isRunning.get();
        }

        public void run() {
            if (!isRunning.compareAndSet(false, true)) {
                Log.debug("Simulation is already running for " + producer.device.hardwareId());
                return;
            }

            Log.debug("Starting simulation for " + producer.device.hardwareId());

            Uni<Void> setupSequence = isRegistered
                ? Uni.createFrom().voidItem()
                : registerAndLogin();

            setupSequence.subscribe().with(_ -> {
                    if (isRunning.get()) {
                        connectAndSchedule();
                    }
                },
                throwable -> {
                    Log.error("Initialization sequence failed for " + producer.device.hardwareId(), throwable);
                    isRunning.set(false);
                }
            );
        }

        public void stop() {
            if (isRunning.compareAndSet(true, false)) {
                Log.info("Stopping sending messages for " + producer.device.hardwareId());

                for (ScheduledFuture<?> task : tasks) {
                    task.cancel(false);
                }
                tasks.clear();
                mqttClient.disconnect();
            }
        }

        private Uni<Void> registerAndLogin() {
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

            return registerDevice(registerRequest)
                .chain(() -> authClient.login(loginRequest))
                .invoke(loginResponse -> {
                    isRegistered = true;
                    producer.deviceId = loginResponse.deviceId();
                    producer.token = loginResponse.token();
                })
                .replaceWithVoid();
        }

        private void connectAndSchedule() {
            if (mqttClient == null) {
                mqttClient = MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(producer.device.hardwareId())
                    .serverHost(brokerHost)
                    .serverPort(brokerPort)
                    .sslConfig()
                        .trustManagerFactory(provider.provide())
                    .applySslConfig()
                    .simpleAuth()
                        .username(producer.deviceId.toString())
                        .password(producer.token.getBytes())
                    .applySimpleAuth()
                    .automaticReconnectWithDefaultConfig()
                    .buildAsync();
            }

            mqttClient.connect().whenComplete((_, throwable) -> {
                if (throwable != null) {
                    Log.error("Connection error: " + brokerHost, throwable);
                    isRunning.set(false);
                    return;
                }

                tasks.add(scheduleTask(producer.messageType, producer::getData, producer.device.topic(), producer.mainTiming));

                if (producer.batteryIsPresent()) {
                    tasks.add(scheduleTask(producer.messageType, producer::getBatteryData, producer.device.batteryTopic(), producer.batteryTiming));
                }
            });
        }

        private ScheduledFuture<?> scheduleTask(
            MessageType messageType,
            Supplier<Object> dataProvider,
            String topic,
            MessageTiming timing
        ) {
            return scheduler.scheduleAtFixedRate(() -> {
                if (!isRunning()) return;
                final var data = dataProvider.get();

                mqttClient.publishWith()
                    .topic(topic)
                    .payload(serialize(data, messageType))
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .messageExpiryInterval(timing.messageTtlSeconds())
                    .send()
                    .whenComplete((_, pubThrowable) -> {
                        if (pubThrowable != null) {
                            Log.error("Send error in topic " + topic, pubThrowable);
                        } else {
                            Log.debug("Published data " + data + " in topic " + topic);
                        }
                    });
            }, timing.initialDelay(), timing.period(), timing.unit());
        }
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

    private byte[] serialize(Object payload, MessageType messageType) {
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
}