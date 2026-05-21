package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.BatteryAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.battery.BatteryDataDeserializer;
import dev.nez.analytics.data.battery.BatteryThresholds;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.dto.proto.timeddata.BatteryData;

import dev.nez.notification.Alert;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

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
        final var batterySerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new BatteryDataDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(BatteryThresholds.class)
        );

        final KTable<Long, BatteryThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde)
        );

        final KStream<Long, BatteryData> batteryStream = builder.stream(
            batteryTopic,
            Consumed.with(longSerde, batterySerde)
        );

        batteryStream
            .join(
                thresholdsTable,
                (event, latestThreshold) -> analyzer.checkThreshold(event, latestThreshold),
                Joined.with(longSerde, batterySerde, thresholdsSerde)
            )
            .filter((id, alertMessage) -> alertMessage != null && filter.apply(id, alertMessage))
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}
