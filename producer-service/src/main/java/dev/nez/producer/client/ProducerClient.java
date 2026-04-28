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
import java.util.concurrent.atomic.AtomicReference;
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
        return new DeviceSession(generator);
    }

    public class DeviceSession {
        private final AtomicReference<CompletableFuture<Void>> future = new AtomicReference<>(null);
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        private final List<ScheduledFuture<?>> tasks = new CopyOnWriteArrayList<>();
        private final DeviceDataGenerator producer;
        private Mqtt5AsyncClient mqttClient;

        private DeviceSession(DeviceDataGenerator producer) {
            this.producer = producer;
            init();
        }

        private void init() {
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

            final var register = authClient.register(registerRequest)
                .onFailure(ClientWebApplicationException.class)
                .recoverWithUni(ex ->
                    ex.getResponse().getStatus() == 409
                        ? Uni.createFrom().item(Response.ok().build())
                        : Uni.createFrom().failure(ex)
                ).replaceWithVoid();

            final var login = authClient.login(loginRequest).invoke(loginResponse -> {
                producer.deviceId = loginResponse.deviceId();
                producer.token = loginResponse.token();
            }).replaceWithVoid();

            future.set(Uni.createFrom().voidItem()
               .chain(_ -> register)
               .chain(_ -> login)
               .invoke(_ ->
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
                       .buildAsync()
               )
               .replaceWithVoid()
               .subscribeAsCompletionStage());
        }

        public float getIntensityPerSecond() {
            return producer.getIntensityPerSecond();
        }

        public boolean isRunning() {
            return isRunning.get();
        }

        public void run() {
            if (!isRunning.compareAndSet(false, true)) {
                return;
            }

            future.set(future.get().thenCompose(_ -> {
                if (!isRunning()) {
                    return CompletableFuture.completedFuture(null);
                }

                Log.debug("Starting simulation for " + producer.device.hardwareId());
                return connectAndSchedule();
            }));
        }

        public void stop() {
            if (!isRunning.compareAndSet(true, false)) {
                return;
            }

            future.set(future.get().thenCompose(_ -> {
                Log.debug("Stopping sending messages for " + producer.device.hardwareId());

                for (ScheduledFuture<?> task : tasks) {
                    task.cancel(false);
                }
                tasks.clear();

                if (mqttClient == null) {
                    return CompletableFuture.completedFuture(null);
                }

                return mqttClient.disconnect().exceptionally(throwable -> {
                    if (!(throwable instanceof MqttSessionExpiredException)) {
                        Log.warn("Disconnect error: " + brokerHost, throwable);
                    }
                    return null;
                });
            }));
        }

        private CompletableFuture<Void> connectAndSchedule() {
            return mqttClient.connect()
                .thenAccept(_ -> {
                    if (!isRunning()) return;

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
                })
                .exceptionally(throwable -> {
                    Log.warn("Connection error: " + brokerHost, throwable);
                    isRunning.set(false);
                    return null;
                });
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