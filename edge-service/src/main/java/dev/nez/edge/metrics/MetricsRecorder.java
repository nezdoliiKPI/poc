package dev.nez.edge.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;

@Singleton
public class MetricsRecorder {
    private final MeterRegistry registry;

    public MetricsRecorder(final MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMessageFilterDrop(String topicName) {
        registry.counter("edge_filter_messages_dropped", "topic", topicName).increment();
    }
}
