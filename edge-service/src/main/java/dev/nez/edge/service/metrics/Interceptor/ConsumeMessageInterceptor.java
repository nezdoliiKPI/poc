package dev.nez.edge.service.metrics.Interceptor;

import dev.nez.edge.service.metrics.recorder.MetricsRecorder;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@SuppressWarnings("unused")
@RecordConsumingMessage(topic = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ConsumeMessageInterceptor {

    @Inject
    MetricsRecorder recorder;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        final RecordConsumingMessage annotation = context.getMethod().getAnnotation(RecordConsumingMessage.class);
        final String channel = annotation.topic();

        recorder.recordMqttMessageReceived(channel);
        return context.proceed();
    }
}
