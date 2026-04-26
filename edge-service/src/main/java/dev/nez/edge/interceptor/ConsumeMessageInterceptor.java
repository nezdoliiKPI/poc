package dev.nez.edge.interceptor;

import dev.nez.edge.exception.MessageParseException;
import dev.nez.edge.metrics.MetricsRecorder;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
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
    public Object intercept(InvocationContext context) throws Exception {
        final var annotation = context.getMethod().getAnnotation(InterceptConsumingMessage.class);
        final var topicName = annotation.value();

        return ((Multi<?>) context.proceed())
            .onFailure().invoke(throwable -> Log.error("Decode message error from topic " + topicName, throwable))
            .onFailure(MessageParseException.class).recoverWithCompletion();
    }
}