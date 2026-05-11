package dev.nez.edge.messaging.consumer;

import dev.nez.edge.exception.MessageParseException;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import io.quarkus.logging.Log;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import io.smallrye.reactive.messaging.TracingMetadata;

import java.util.function.Function;

public abstract class BaseMqttConsumer<T> {
    private final Function<T, Long> getDeviceId;

    @Inject
    Tracer tracer;

    public BaseMqttConsumer(Function<T, Long> getDeviceId) {
        this.getDeviceId = getDeviceId;
    }

    protected Multi<Message<T>> consume(
        Multi<Message<byte[]>> stream,
        Function<byte[], T> map,
        ChannelFilter<T> filter,
        String topicName
    ) {
        return stream.onItem().transformToMultiAndConcatenate(msg -> {
            final var controller =  new SpanController(msg, topicName);

            if (!filter.shouldConsume()) {
                controller.end("dropped_by_topic_filter");
                return drop(msg);
            }

            T data;
            try {
                data = map.apply(msg.getPayload());
            } catch (final MessageParseException e) {
                controller.endWithError(e, "Decode message error from topic " + topicName + ":");
                return drop(msg);
            }

            final Long deviceId = getDeviceId.apply(data);
            if (!filter.apply(deviceId, data)) {
                controller.end("dropped_by_topic_filter_optimizer");
                return drop(msg);
            }

            final Message<T> outgoingMessage = msg.withPayload(data)
                .withMetadata(
                    msg.getMetadata()
                        .with(controller.getTracingMetadata())
                        .with(OutgoingKafkaRecordMetadata.<Long>builder()
                                  .withKey(deviceId)
                                  .build())
                )
                .withAck(() -> {
                    controller.end();
                    return msg.ack();
                })
                .withNack(reason -> {
                    controller.endWithError(reason);
                    return msg.nack(reason);
                });

            return Multi.createFrom().item(outgoingMessage);
        });
    }

    private Multi<Message<T>> drop(Message<?> msg) {
        return Uni.createFrom().completionStage(msg.ack())
            .onItem().transformToMulti(ignored -> Multi.createFrom().empty());
    }

    private class SpanController {
        private final Context parentContext;
        private final Span span;

        SpanController(Message<?> msg, String topicName) {
            parentContext = msg.getMetadata(TracingMetadata.class)
                .map(TracingMetadata::getCurrentContext)
                .orElseGet(Context::current);

            span = tracer.spanBuilder("consume-" + topicName)
                .setParent(parentContext)
                .startSpan();
        }

        TracingMetadata getTracingMetadata() {
            Context otelContext = parentContext.with(span);
            return TracingMetadata.withCurrent(otelContext);
        }

        void end() {
            span.end();
        }

        void end(String endAction) {
            span.setAttribute("action", endAction);
            span.end();
        }

        void endWithError(Throwable throwable, String logError) {
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
            Log.error(logError, throwable);
            span.end();
        }

        void endWithError(Throwable throwable) {
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
            span.end();
        }
    }
}
