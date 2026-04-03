package dev.nez.edge.connector;

import dev.nez.edge.connector.MqttConnector.FailureStrategy;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public record HiveMqttMessage(
    Mqtt5Publish publish,
    FailureStrategy failureStrategy
) implements Message<byte[]> {

    @Override
    public byte[] getPayload() {
        return publish.getPayloadAsBytes();
    }


    @Override
    public CompletionStage<Void> ack() {
        publish.acknowledge();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason) {
        return switch (failureStrategy) {
            case ignore -> CompletableFuture.completedFuture(null);
            case fail -> CompletableFuture.failedFuture(reason);
        };
    }
}
