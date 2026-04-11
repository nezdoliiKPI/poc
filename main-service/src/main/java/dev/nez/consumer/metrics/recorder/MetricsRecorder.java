package dev.nez.consumer.metrics.recorder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MetricsRecorder {
    private final MeterRegistry registry;

    public MetricsRecorder(final MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMessageProcessingError(String topicName, String exceptionType) {
        registry.counter(
        "mqtt_messages_processing_errors_total",
        "topic", topicName,
            "error_type", exceptionType
        ).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordProcessingTime(Timer.Sample sample, String topicName) {
        Timer timer = Timer.builder("mqtt_message_processing_duration")
                .description("Time taken to process MQTT messages")
                .tags("topic", topicName)
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                    Duration.ofMillis(5),
                    Duration.ofMillis(20),
                    Duration.ofMillis(100),
                    Duration.ofMillis(500)
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMillis(2000))
                .register(registry);

        sample.stop(timer);
    }
}
