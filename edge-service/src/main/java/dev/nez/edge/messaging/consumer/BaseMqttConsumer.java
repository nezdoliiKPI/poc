package dev.nez.edge.messaging.consumer;

import dev.nez.edge.exception.MessageParseException;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.function.Function;

public abstract class BaseMqttConsumer<T> {
    private final Function<T, Long> getDeviceId;

    public BaseMqttConsumer(Function<T, Long> getDeviceId) {
        this.getDeviceId = getDeviceId;
    }

    protected Multi<Message<T>> consume(
        Multi<Message<byte[]>> stream,
        Function<byte[], T> map,
        ChannelFilter<T> filter
    ) {
        return stream.onItem().transformToMultiAndConcatenate(msg -> {
            if (!filter.shouldConsume()) {
                return drop(msg);
            }

            T data;
            try {
                data = map.apply(msg.getPayload());
            } catch (final MessageParseException e) {
                return drop(msg);
            }

            final Long deviceId = getDeviceId.apply(data);
            if (!filter.apply(deviceId, data)) {
                return drop(msg);
            }

            final Message<T> outgoingMessage = msg.withPayload(data)
                .withMetadata(
                    msg.getMetadata()
                        .with(OutgoingKafkaRecordMetadata.<Long>builder()
                                  .withKey(deviceId)
                                  .build())
                )
                .withAck(msg::ack)
                .withNack(msg::nack);

            return Multi.createFrom().item(outgoingMessage);
        });
    }

    private Multi<Message<T>> drop(Message<?> msg) {
        return Uni.createFrom().completionStage(msg.ack())
            .onItem().transformToMulti(ignored -> Multi.createFrom().empty());
    }
}
