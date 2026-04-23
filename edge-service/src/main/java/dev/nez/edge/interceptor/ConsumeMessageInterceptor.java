package dev.nez.edge.interceptor;

import dev.nez.edge.exception.MessageParseException;
import dev.nez.edge.metrics.recorder.MetricsRecorder;

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
@InterceptConsumingMessage("")
@Priority(Interceptor.Priority.APPLICATION)
public class ConsumeMessageInterceptor {

    @Inject
    MetricsRecorder recorder;

    @AroundInvoke
    public Object intercept(InvocationContext context) {
        final var annotation = context.getMethod().getAnnotation(InterceptConsumingMessage.class);
        final String topicName = annotation.value();

        return Uni.createFrom().deferred(Unchecked.supplier(() -> {
            final var timer = recorder.startTimer();

            return ((Uni<?>) context.proceed())
                .eventually(() -> recorder.recordProcessingTime(timer, topicName))
                .onFailure().invoke(throwable -> {
                    recorder.recordMessageProcessingError(topicName, throwable.getClass().getSimpleName());
                    Log.error("Decode message error from topic " + topicName, throwable);
                })
                .onFailure(MessageParseException.class).recoverWithNull();
        }));
    }
}