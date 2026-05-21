package dev.nez.analytics.topology.stream;
import dev.nez.analytics.analyzer.AirQualityAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.air.*;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.AirQualityData;

import dev.nez.notification.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class AirQualityStream extends TelemetryStreamBase {

    @ConfigProperty(name = "kafka.topic.air.events")
    String airQualityTopic;

    @ConfigProperty(name = "kafka.topic.air.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    AirQualityAnalyzer analyzer;

    @Inject
    public AirQualityStream(NotificationFilter notificationFilter) {
        super(notificationFilter);
    }

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();

        final var alertSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Alert.class)
        );
        final var airQualitySerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new AirQualityDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(AirQualityThresholds.class)
        );

        final KTable<Long, AirQualityThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde)
        );

        final KStream<Long, AirQualityData> airQualityStream = builder.stream(
            airQualityTopic,
            Consumed.with(longSerde, airQualitySerde)
        );

        airQualityStream
            .join(
                thresholdsTable,
                (event, latestThreshold) -> analyzer.checkThreshold(event, latestThreshold),
                Joined.with(longSerde, airQualitySerde, thresholdsSerde)
            )
            .filter((id, alertMessage) -> alertMessage != null && filter.apply(id, alertMessage))
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}
