package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.AirQualityAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.air.*;

import dev.nez.analytics.data.alert.AlertDeserializer;
import dev.nez.analytics.data.alert.AlertSerializer;
import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.AirQualityData;

import dev.nez.analytics.data.alert.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;

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
            new AlertSerializer(),
            new AlertDeserializer()
        );
        final var dataSerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new AirQualityDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(AirQualityThresholds.class)
        );

        final KTable<Long, AirQualityThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde),
            Materialized.<Long, AirQualityThresholds>as(
                Stores.inMemoryKeyValueStore("air-quality-thresholds-store")
            ).withKeySerde(longSerde).withValueSerde(thresholdsSerde)
        );

        final KStream<Long, AirQualityData> airQualityStream = builder.stream(
            airQualityTopic,
            Consumed.with(longSerde, dataSerde)
        );

        airQualityStream
            .join(
                thresholdsTable,
                (event, latestThreshold) -> analyzer.checkThreshold(event, latestThreshold),
                Joined.with(longSerde, dataSerde, thresholdsSerde)
            )
            .flatMapValues(alerts -> alerts != null ? alerts : java.util.Collections.emptyList())
            .filter((_, alert) -> alert != null && filter.apply(alert))
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}