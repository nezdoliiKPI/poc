package dev.nez.edge.connector;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;

import io.quarkus.logging.Log;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.connector.InboundConnector;

import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.vertx.core.Vertx;
import io.vertx.core.Context;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import mutiny.zero.flow.adapters.AdaptersToFlow;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

@ApplicationScoped
@Connector(MqttConnector.CONNECTOR_NAME)
public class MqttConnector  implements InboundConnector {
    public static final String CONNECTOR_NAME = "mqtt-hivemq";

    private final List<Mqtt5RxClient> clients = new CopyOnWriteArrayList<>();

    public enum FailureStrategy {
        ignore, fail
    }

    @Inject
    Vertx vertx;

    @Override
    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    public Flow.Publisher<? extends Message<?>> getPublisher(Config config) {
        final String channelName = config.getOptionalValue("channel-name", String.class)
                .orElseThrow(() -> new IllegalArgumentException("channel-name not found"));

        final String host = config.getOptionalValue("host", String.class)
                .orElseThrow(() -> new IllegalArgumentException("host not found"));

        final Integer port = config.getOptionalValue("port", Integer.class)
                .orElseThrow(() -> new IllegalArgumentException("port not found"));

        final String topic = config.getOptionalValue("topic", String.class)
                .orElseThrow(() -> new IllegalArgumentException("topic not found"));

        final FailureStrategy failureStrategy = config.getOptionalValue("failure-strategy", FailureStrategy.class)
                .orElse(FailureStrategy.fail);

        final String clientId = config.getOptionalValue("client-id", String.class)
                .orElseThrow(() -> new IllegalArgumentException("client-id MUST be configured for persistent sessions"))
                + "-" + channelName;

        final MqttQos qos = config.getOptionalValue("qos", Integer.class)
                .map(code -> Optional.ofNullable(MqttQos.fromCode(code))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid QoS value: " + code + ".")))
                .orElseThrow(() -> new IllegalArgumentException("qos not found"));

        final Long sessionExpiryInterval = config.getOptionalValue("session-expiry-interval", Long.class)
                .orElse(24 * 3600L);


        final Mqtt5RxClient client = Mqtt5Client.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .automaticReconnectWithDefaultConfig()
                .buildRx();

        clients.add(client);

        final Flowable<HiveMqttMessage> messagesFlowable = client.publishes(MqttGlobalPublishFilter.ALL, true)
                .map(publish -> new HiveMqttMessage(publish, failureStrategy));

        final Completable setupCompletable = client.connectWith()
                .cleanStart(false)
                .sessionExpiryInterval(sessionExpiryInterval)
                .applyConnect()
                .flatMapCompletable(connAck -> {
                    if (connAck.isSessionPresent()) {
                        Log.infof("Connected to broker. Resumed session for %s. Skipping re-subscription on %s", clientId, topic);
                        return Completable.complete();
                    } else {
                        Log.infof("Connected to broker. Started new session for %s. Subscribing to %s...", clientId, topic);
                        return client.subscribeWith()
                                .topicFilter(topic)
                                .qos(qos)
                                .applySubscribe()
                                .doOnSuccess(_ -> Log.infof("Successfully subscribed to topic: %s with QoS: %s", topic, qos))
                                .ignoreElement();
                    }
                });

        final Context rootContext = vertx.getOrCreateContext();

        final Flowable<HiveMqttMessage> rxFlowable = Flowable.merge(messagesFlowable, setupCompletable.toFlowable())
                .doOnError(throwable -> Log.errorf(throwable, "Fatal error in MQTT reactive stream for channel: %s", channelName));

        return Multi.createFrom().publisher(AdaptersToFlow.publisher(rxFlowable)).emitOn(
                runnable -> rootContext.runOnContext(
                        _ -> VertxContext.createNewDuplicatedContext().runOnContext(
                                _ -> runnable.run())));
    }

    @PreDestroy
    public void cleanup() {
        if (clients.isEmpty()) {
            return;
        } else {
            Log.info("Application is shutting down. Disconnecting MQTT clients...");
        }

        final List<Completable> disconnectTasks = clients.stream().map(client -> {
            final String clientId = client.getConfig().getClientIdentifier()
                    .map(Object::toString)
                    .orElse("unknown");

            return client.disconnect()
                    .doOnComplete(() -> Log.infof("Successfully disconnected MQTT client: %s", clientId))
                    .doOnError(e -> Log.errorf(e, "Error while disconnecting MQTT client: %s", clientId))
                    .onErrorComplete();
        }).collect(Collectors.toList());

        Completable.merge(disconnectTasks).blockingAwait();

        clients.clear();
        Log.info("MQTT clients cleanup process finished.");
    }
}




