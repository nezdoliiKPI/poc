package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.PowerConsumptionAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.power.PowerConsumptionDeserializer;
import dev.nez.analytics.data.power.PowerThresholds;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import dev.nez.analytics.data.alert.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class PowerConsumptionStream extends TelemetryStreamBase {

    @ConfigProperty(name = "kafka.topic.power.events")
    String consumptionTopic;

    @ConfigProperty(name = "kafka.topic.power.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    PowerConsumptionAnalyzer analyzer;

    @Inject
    public PowerConsumptionStream(NotificationFilter notificationFilter) {
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
            new PowerConsumptionDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(PowerThresholds.class)
        );

        final KTable<Long, PowerThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde),
            Materialized.<Long, PowerThresholds>as(
                Stores.inMemoryKeyValueStore("power-thresholds-store")
            ).withKeySerde(longSerde).withValueSerde(thresholdsSerde)
        );

        final KStream<Long, PowerConsumptionData> consumptionStream = builder.stream(
            consumptionTopic,
            Consumed.with(longSerde, dataSerde)
        );

        consumptionStream
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