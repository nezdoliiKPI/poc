package dev.nez.edge.interceptor;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import dev.nez.edge.exception.DecodeMessageException;
import dev.nez.edge.service.metrics.recorder.MetricsRecorder;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@SuppressWarnings({
    "ReactiveStreamsUnusedPublisher",
    "unused"
})
@Interceptor
@RecordConsumingMessage("")
@Priority(Interceptor.Priority.APPLICATION)
public class ConsumeMessageInterceptor {
    private static final Printer protoPrinter =
        JsonFormat.printer().omittingInsignificantWhitespace();

    @Inject
    MetricsRecorder recorder;

    @AroundInvoke
    public Object intercept(InvocationContext context) {
        final RecordConsumingMessage annotation = context.getMethod().getAnnotation(RecordConsumingMessage.class);
        final String topicName = annotation.value();

        return Uni.createFrom().deferred(Unchecked.supplier(() -> {
            final var timer = recorder.startTimer();

            return ((Uni<?>) context.proceed())
                .invoke(telemetry -> {
                    if (Log.isDebugEnabled()) {
                        try {
                            Log.debug("Received from " + topicName + ": " + protoPrinter.print((MessageOrBuilder) telemetry));
                        } catch (final InvalidProtocolBufferException e) {
                            Log.warn("Failed to parse telemetry to JSON for debug log", e);
                        }
                    }
                })
                .eventually(() -> recorder.recordProcessingTime(timer, topicName))
                .onFailure().invoke(throwable -> {
                    recorder.recordMessageProcessingError(topicName, throwable.getClass().getSimpleName());
                    Log.error("Decode message error from topic " + topicName, throwable);
                })
                .onFailure(DecodeMessageException.class).recoverWithNull();
        }));
    }
}