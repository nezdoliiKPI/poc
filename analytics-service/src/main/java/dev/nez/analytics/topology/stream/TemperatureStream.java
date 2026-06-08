package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.TemperatureAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.alert.AlertDeserializer;
import dev.nez.analytics.data.alert.AlertSerializer;
import dev.nez.analytics.data.temperature.*;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.TemperatureData;

import dev.nez.analytics.data.alert.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class TemperatureStream extends TelemetryStreamBase {

    @ConfigProperty(name = "kafka.topic.temp.events")
    String tempTopic;

    @ConfigProperty(name = "kafka.topic.temp.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    TemperatureAnalyzer analyzer;

    @Inject
    public TemperatureStream(NotificationFilter notificationFilter) {
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
            new TemperatureDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(TemperatureThresholds.class)
        );

        final KTable<Long, TemperatureThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde),
            Materialized.<Long, TemperatureThresholds>as(
                Stores.inMemoryKeyValueStore("temperature-thresholds-store")
            ).withKeySerde(longSerde).withValueSerde(thresholdsSerde)
        );

        final KStream<Long, TemperatureData> tempStream = builder.stream(
            tempTopic,
            Consumed.with(longSerde, dataSerde)
        );

        tempStream
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