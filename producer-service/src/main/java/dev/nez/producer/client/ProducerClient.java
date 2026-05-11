package dev.nez.producer.client;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.producer.dto.rest.LoginRequest;
import dev.nez.producer.dto.rest.PowerThresholdsRequest;
import dev.nez.producer.dto.rest.RegisterRequest;

import dev.nez.producer.simulation.generator.data.AirDataGenerator;
import dev.nez.producer.simulation.generator.data.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.data.DeviceDataGenerator.MessageType;
import dev.nez.producer.simulation.generator.data.PowerDataGenerator;
import dev.nez.producer.simulation.generator.data.SmokeDataGenerator;
import dev.nez.producer.simulation.model.MessageTiming;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.mqtt.MqttClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@ApplicationScoped
public class ProducerClient {
    private final List<DeviceSession> activeSessions = new CopyOnWriteArrayList<>();

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;

    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    @ConfigProperty(name = "mqtt.broker.ca-path", defaultValue = "../secrets/tls/ca.crt")
    String caPath;

    @RestClient
    AuthRestClient authClient;

    @Inject
    Vertx vertx;

    public DeviceSession createSession(DeviceDataGenerator generator) {
        DeviceSession session = new DeviceSession(generator).init();
        activeSessions.add(session);
        return session;
    }

    void onStop(@Observes ShutdownEvent ev) {
        Log.info("Shutting down ProducerClient. Stopping all sessions...");
        activeSessions.forEach(DeviceSession::shutdown);
    }

    public class DeviceSession {
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final List<Long> timers = new CopyOnWriteArrayList<>();
        private final DeviceDataGenerator producer;

        private MqttClient mqttClient;

        private DeviceSession(DeviceDataGenerator producer) {
            this.producer = producer;
        }

        public float getIntensityPerSecond() {
            return producer.getIntensityPerSecond();
        }

        public boolean isRunning() {
            return isRunning.get();
        }

        private DeviceSession init() {
            Uni.createFrom().voidItem()
                .chain(this::register)
                .chain(this::login)
                .chain(this::setTreshold)
                .chain(this::connectAndSchedule)
                .subscribe().with(
                    _ -> Log.debug("Device session has been initialized for device: " + producer.deviceId),
                    ex -> {
                        Log.error("Failed init device " + producer.deviceId + " simulation", ex);
                        cancelTimers();
                    }
                );

            return this;
        }

        private Uni<Void> register() {
            final var registerRequest = new RegisterRequest(
                producer.device.hardwareId(),
                producer.device.password(),
                producer.device.topic(),
                producer.device.batteryTopic(),
                producer.messageType
            );

            return authClient.register(registerRequest)
                .onFailure(ClientWebApplicationException.class)
                .recoverWithUni(ex ->
                                    ex.getResponse().getStatus() == 409
                                        ? Uni.createFrom().item(Response.ok().build())
                                        : Uni.createFrom().failure(ex)
                )
                .replaceWithVoid();
        }

        private Uni<Void> login() {
            final var loginRequest = new LoginRequest(
                producer.device.hardwareId(),
                producer.device.password()
            );

            return authClient.login(loginRequest)
                .invoke(loginResponse -> {
                    producer.deviceId = loginResponse.deviceId();
                    producer.token = loginResponse.token();
                })
                .replaceWithVoid();
        }

        private Uni<Void> setTreshold() {
            return switch (producer) {
                case PowerDataGenerator g -> {
                    final var defaultThresholds = new PowerThresholdsRequest(
                        producer.deviceId,
                        207.0f,
                        253.0f,
                        16.0f,
                        3680.0f
                    );

                    yield authClient.setThresholds(defaultThresholds).replaceWithVoid();
                }
                default -> Uni.createFrom().voidItem();
            };
        }

        private Uni<Void> connectAndSchedule() {
            MqttClientOptions options = new MqttClientOptions()
                .setClientId(producer.device.hardwareId())
                .setUsername(producer.deviceId.toString())
                .setPassword(producer.token)
                .setSsl(true)
                .setPemTrustOptions(new PemTrustOptions().addCertPath(caPath))
                .setHostnameVerificationAlgorithm("HTTPS")
                .setAutoKeepAlive(true);

            this.mqttClient = MqttClient.create(vertx, options);

            return mqttClient.connect(brokerPort, brokerHost)
                .invoke(_ -> {
                    timers.add(scheduleTask(
                        producer.messageType,
                        producer::getData,
                        producer.device.topic(),
                        producer.mainTiming
                    ));

                    if (!producer.batteryIsPresent()) {
                        return;
                    }

                    timers.add(scheduleTask(
                        producer.messageType,
                        producer::getBatteryData,
                        producer.device.batteryTopic(),
                        producer.batteryTiming
                    ));
                })
                .replaceWithVoid();
        }

        public void run() {
            if (!isRunning.compareAndSet(false, true)) {
                return;
            }

            Log.debug("Starting simulation for " + producer.device.hardwareId());
        }

        public void stop() {
            if (!isRunning.compareAndSet(true, false)) {
                return;
            }

            Log.debug("Stopping sending messages for " + producer.device.hardwareId());
        }

        private void shutdown() {
            stop();
            cancelTimers();

            if (mqttClient != null) {
                mqttClient.disconnect().subscribe().with(
                    _ -> Log.debug("Disconnected MQTT client for " + producer.device.hardwareId()),
                    ex -> Log.warn("Disconnect error", ex)
                );
            }
        }

        private void cancelTimers() {
            timers.forEach(vertx::cancelTimer);
            timers.clear();
        }

        private long scheduleTask(
            MessageType messageType,
            Supplier<Object> dataProvider,
            String topic,
            MessageTiming timing
        ) {
            final long initialDelayMs = timing.unit().toMillis(timing.initialDelay());
            final long periodMs = timing.unit().toMillis(timing.period());

            final boolean isDebug = Log.isDebugEnabled();

            return vertx.setPeriodic(initialDelayMs, periodMs, _ -> {
                if (!isRunning()) {
                    return;
                }

                final var data = dataProvider.get();
                final var buffer = Buffer.buffer(serialize(data, messageType));

                final var uni = mqttClient.publish(topic, buffer, MqttQoS.AT_LEAST_ONCE, false, false);
                if (isDebug) {
                    uni.subscribe().with(
                        _ -> Log.debug("Published data " + data + " in topic " + topic),
                        throwable -> Log.warn("Send error in topic " + topic, throwable)
                    );
                } else {
                    uni.subscribe().with(
                        _ -> {},
                        throwable -> Log.warn("Send error in topic " + topic, throwable)
                    );
                }
            });
        }
    }

    private byte[] serialize(Object payload, MessageType messageType) {
        return switch (messageType) {
            case JSON -> Json.encodeToBuffer(payload).getBytes();
            case PROTO -> {
                if (payload instanceof ProtocolBuffer b) {
                    yield b.serialize();
                }
                throw new RuntimeException("Unexpected data type: " + payload.getClass());
            }
        };
    }
}