package dev.nez.producer.client;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
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

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
        private final DeviceDataGenerator producer;

        private final AtomicReference<CompletableFuture<Void>> future = new AtomicReference<>();
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final AtomicBoolean isRegistered = new AtomicBoolean(false);

        private final ArrayList<ScheduledFuture<?>> tasks = new ArrayList<>();
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

        public void run() {
            if (!isRunning.compareAndSet(false, true)) {
                return;
            }

            Log.debug("Starting simulation for " + producer.device.hardwareId());

            Uni<Void> setupSequence = isRegistered.get()
                ? Uni.createFrom().voidItem()
                : registerAndLogin();

            future.set(setupSequence.subscribeAsCompletionStage().whenComplete((_, throwable) -> {
                if (throwable != null) {
                    Log.error("Initialization sequence failed for " + producer.device.hardwareId(), throwable);
                    isRunning.set(false);
                } else if (isRunning.get()) {
                    connectAndSchedule();
                }
            }));
        }

        public void stop() {
            if (!isRunning.compareAndSet(true, false)) {
                return;
            }

            Log.debug("Stopping sending messages for " + producer.device.hardwareId());

            future.getAndSet(null).join();

            for (ScheduledFuture<?> task : tasks) {
                task.cancel(false);
            }
            tasks.clear();

            if (mqttClient != null) {
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

            return Uni.createFrom().voidItem()
                .chain(() -> authClient.register(registerRequest)
                    .onFailure(ClientWebApplicationException.class)
                    .recoverWithUni(ex ->
                        ex.getResponse().getStatus() == 409
                            ?   Uni.createFrom().item(Response.ok().build())
                            :   Uni.createFrom().failure(ex)
                ))
                .chain(() -> authClient.login(loginRequest))
                .invoke(loginResponse -> {
                    isRegistered.set(true);
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