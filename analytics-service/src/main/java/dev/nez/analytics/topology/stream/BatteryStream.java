package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.BatteryAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.battery.BatteryDataDeserializer;
import dev.nez.analytics.data.battery.BatteryThresholds;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.BatteryData;

import dev.nez.analytics.data.alert.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class BatteryStream extends TelemetryStreamBase {

    @ConfigProperty(name = "kafka.topic.battery.events")
    String batteryTopic;

    @ConfigProperty(name = "kafka.topic.battery.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    BatteryAnalyzer analyzer;

    @Inject
    public BatteryStream(NotificationFilter notificationFilter) {
        super(notificationFilter);
    }

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();

        final var alertSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Alert.class)
        );
        final var dataSerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new BatteryDataDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(BatteryThresholds.class)
        );

        final KTable<Long, BatteryThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde),
            Materialized.<Long, BatteryThresholds>as(
                Stores.inMemoryKeyValueStore("battery-thresholds-store")
            ).withKeySerde(longSerde).withValueSerde(thresholdsSerde)
        );

        final KStream<Long, BatteryData> batteryStream = builder.stream(
            batteryTopic,
            Consumed.with(longSerde, dataSerde)
        );

        batteryStream
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