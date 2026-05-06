package dev.nez.edge.messaging.consumer;

import dev.nez.edge.exception.MessageParseException;

import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;

import io.quarkus.logging.Log;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.function.Function;

public abstract class MqttConsumer <T> {
    private final Function<T, Long> getDeviceId;

    public MqttConsumer(Function<T, Long> getDeviceId) {
        this.getDeviceId = getDeviceId;
    }

    Multi<Message<T>> consume(
        Multi<Message<byte[]>> stream,
        Function<byte[], T> map,
        ChannelFilter<T> filter,
        String topicName
    ) {
        return stream
            .filter(msg -> {
                if (!filter.shouldConsume()) {
                    msg.ack();
                    return false;
                }
                return true;
            })
            .onItem().transformToMultiAndConcatenate(item -> Uni.createFrom().item(item)
                .map(msg -> msg.withPayload(map.apply(msg.getPayload())))
                .toMulti()
                .onFailure(MessageParseException.class)
                .recoverWithMulti(throwable -> {
                    Log.error("Decode message error from topic " + topicName, throwable);
                    item.ack();
                    return Multi.createFrom().empty();
                }))
            .filter(msg -> {
                var data = msg.getPayload();

                if (!filter.apply(getDeviceId.apply(data), data)) {
                    msg.ack();
                    return false;
                }
                return true;
            });
    }
}
