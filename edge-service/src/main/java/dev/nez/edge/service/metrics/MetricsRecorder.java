package dev.nez.edge.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetricsRecorder {
    private final MeterRegistry registry;

    public MetricsRecorder(final MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMqttMessageReceived(String topicName) {
        registry.counter("mqtt_messages_received_total", "topic", topicName)
                .increment();
    }
}
