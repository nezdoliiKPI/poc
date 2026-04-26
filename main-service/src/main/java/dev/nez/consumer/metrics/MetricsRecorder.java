package dev.nez.consumer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;

@Singleton
public class MetricsRecorder {
    private final MeterRegistry registry;

    public MetricsRecorder(final MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMessagesProcessed(String channelName, int count) {
        registry.counter(
            "messages_processed",
            "channel", channelName
        ).increment(count);
    }
}
