package dev.nez.consumer.interceptor;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import dev.nez.consumer.metrics.recorder.MetricsRecorder;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

// TODO: Now is really unused
@SuppressWarnings({
    "ReactiveStreamsUnusedPublisher",
    "unused"
})
@Interceptor
@InterceptConsumingMessage("")
@Priority(Interceptor.Priority.APPLICATION)
public class ConsumeMessageInterceptor {
    private static final Printer protoPrinter = JsonFormat.printer().omittingInsignificantWhitespace();

    @Inject
    MetricsRecorder recorder;

    @AroundInvoke
    public Object intercept(InvocationContext context) {
        final InterceptConsumingMessage annotation = context.getMethod().getAnnotation(InterceptConsumingMessage.class);
        final String topicName = annotation.value();

        return Uni.createFrom().deferred(Unchecked.supplier(() -> {
            final var timer = recorder.startTimer();

            return ((Uni<?>) context.proceed())
                .invoke(Unchecked.consumer(telemetry -> {
                    if (Log.isDebugEnabled()) {
                        Log.debug("Received from " + topicName + ": " + protoPrinter.print((MessageOrBuilder) telemetry));
                    }
                }))
                .eventually(() -> recorder.recordProcessingTime(timer, topicName))
                .onFailure().invoke(throwable -> {
                    recorder.recordMessageProcessingError(topicName, throwable.getClass().getSimpleName());
                    Log.error("Processing message error from topic " + topicName, throwable);
                })
                .onFailure().recoverWithNull();
        }));
    }
}