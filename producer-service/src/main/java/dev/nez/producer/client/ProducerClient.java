package dev.nez.producer.client;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import dev.nez.producer.simulation.generator.data.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.data.DeviceDataGenerator.MessageType;

import dev.nez.producer.dto.rest.LoginRequest;
import dev.nez.producer.dto.rest.RegisterRequest;
import dev.nez.producer.dto.ProtocolBuffer;

import dev.nez.producer.simulation.model.MessageTiming;
import dev.nez.producer.security.MqttTrustManagerProvider;

import io.quarkus.logging.Log;

import io.smallrye.mutiny.Uni;

import io.vertx.core.json.Json;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@ApplicationScoped
public class ProducerClient {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String brokerHost;
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int brokerPort;

    @RestClient
    AuthRestClient authClient;

    @Inject
    MqttTrustManagerProvider provider;

    public DeviceSession createSession(DeviceDataGenerator generator) {
        return new DeviceSession(generator).init();
    }

    public class DeviceSession {
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final List<ScheduledFuture<?>> tasks = new CopyOnWriteArrayList<>();
        private final DeviceDataGenerator producer;
        private Mqtt5AsyncClient mqttClient;

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
                .chain(_ -> register())
                .chain(_ -> login())
                .chain(_ -> connectAndSchedule())
                .subscribe().with(
                    _ -> Log.debug("Device session has been initialized for device: " + producer.deviceId),
                    ex -> {
                        Log.error("Failed init device" + producer.deviceId + " simulation: " + ex);

                        tasks.forEach(task -> task.cancel(true));
                        tasks.clear();
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
                ).replaceWithVoid();
        }

        private Uni<Void> login() {
            final var loginRequest = new LoginRequest(
                producer.device.hardwareId(),
                producer.device.password()
            );

            return authClient.login(loginRequest).invoke(loginResponse -> {
                producer.deviceId = loginResponse.deviceId();
                producer.token = loginResponse.token();
            }).replaceWithVoid();
        }

        private Uni<Void> connectAndSchedule() {
            this.mqttClient = MqttClient.builder()
                .useMqttVersion5()
                .identifier(producer.device.hardwareId())
                .serverHost(brokerHost)
                .serverPort(brokerPort)
                .sslConfig()
                    .trustManagerFactory(provider.provide()) //or InsecureTrustManagerFactory.INSTANCE
                .applySslConfig()
                .simpleAuth()
                    .username(producer.deviceId.toString())
                    .password(producer.token.getBytes())
                .applySimpleAuth()
                .automaticReconnectWithDefaultConfig()
                .buildAsync();

            return Uni.createFrom().completionStage(mqttClient.connect()
                .thenAccept(_ -> {
                    tasks.add(scheduleTask(
                        producer.messageType,
                        producer::getData,
                        producer.device.topic(),
                        producer.mainTiming
                    ));

                    if (producer.batteryIsPresent()) {
                        tasks.add(scheduleTask(
                            producer.messageType,
                            producer::getBatteryData,
                            producer.device.batteryTopic(),
                            producer.batteryTiming
                        ));
                    }
                }));
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

        private ScheduledFuture<?> scheduleTask(
            MessageType messageType,
            Supplier<Object> dataProvider,
            String topic,
            MessageTiming timing
        ) {
            return scheduler.scheduleAtFixedRate(() -> {
                if (!isRunning()) {
                    return;
                }

                final var data = dataProvider.get();

                mqttClient.publishWith()
                    .topic(topic)
                    .payload(serialize(data, messageType))
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .messageExpiryInterval(timing.messageTtlSeconds())
                    .send()
                    .whenComplete((_, throwable) -> {
                        if (throwable != null &&  !(throwable instanceof MqttSessionExpiredException)) {
                            Log.warn("Send error in topic " + topic, throwable);
                        } else {
                            Log.debug("Published data " + data + " in topic " + topic);
                        }
                    });
            }, timing.initialDelay(), timing.period(), timing.unit());
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