package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.SmokeDetectorAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.smoke.*;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;

import dev.nez.notification.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class SmokeDetectorStream extends TelemetryStreamBase {

    @ConfigProperty(name = "kafka.topic.smoke.events")
    String smokeTopic;

    @ConfigProperty(name = "kafka.topic.smoke.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    SmokeDetectorAnalyzer analyzer;

    @Inject
    public SmokeDetectorStream(NotificationFilter notificationFilter) {
        super(notificationFilter);
    }

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();

        final var alertSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Alert.class)
        );
        final var smokeSerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new SmokeDetectorDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(SmokeDetectorThresholds.class)
        );

        final KTable<Long, SmokeDetectorThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde),
            Materialized.<Long, SmokeDetectorThresholds>as(
                Stores.inMemoryKeyValueStore("smoke-thresholds-store")
            ).withKeySerde(longSerde).withValueSerde(thresholdsSerde)
        );

        final KStream<Long, SmokeDetectorData> smokeStream = builder.stream(
            smokeTopic,
            Consumed.with(longSerde, smokeSerde)
        );

        smokeStream
            .join(
                thresholdsTable,
                (event, latestThreshold) -> analyzer.checkThreshold(event, latestThreshold),
                Joined.with(longSerde, smokeSerde, thresholdsSerde)
            )
            .filter((id, alertMessage) -> alertMessage != null && filter.apply(id, alertMessage))
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}