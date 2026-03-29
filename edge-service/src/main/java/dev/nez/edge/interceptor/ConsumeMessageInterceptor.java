package dev.nez.edge.interceptor;

import dev.nez.edge.exception.DecodeMessageException;
import dev.nez.edge.service.metrics.recorder.MetricsRecorder;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
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

    @Inject
    MetricsRecorder recorder;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        final RecordConsumingMessage annotation = context.getMethod().getAnnotation(RecordConsumingMessage.class);
        final String topicName = annotation.value();

        final var timer = recorder.startTimer();

        return ((Uni<?>)context.proceed())
                    .invoke(telemetry -> Log.debug("Received from " + topicName + ": " + telemetry))
                    .eventually(() -> recorder.recordProcessingTime(timer, topicName))
                    .onFailure().invoke(throwable -> {
                        recorder.recordMessageProcessingError(topicName, throwable.getClass().getSimpleName());
                        Log.error("Decode message error from topic " + topicName + ": " + throwable.getMessage());
                    })
                    .onFailure(DecodeMessageException.class).recoverWithNull();
    }
}