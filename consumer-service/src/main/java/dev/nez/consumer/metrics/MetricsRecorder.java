package dev.nez.consumer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class MetricsRecorder {
    private final MeterRegistry registry;

    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public MetricsRecorder(final MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMessageDelay(String channelName, Duration delay) {
        final Timer timer = timerCache.computeIfAbsent(channelName, channel ->
            Timer.builder("message_delay")
                .tag("channel", channel)
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                    Duration.ofMillis(1),
                    Duration.ofMillis(5),
                    Duration.ofMillis(15),
                    Duration.ofMillis(50),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2)
                )
                .register(registry)
        );

        timer.record(delay);
    }
}
