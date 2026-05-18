package dev.nez.analytics.topology.stream;

import dev.nez.alert.AlertDeserializer;
import dev.nez.alert.AlertSerializer;
import dev.nez.analytics.analyzer.PowerConsumptionAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.battery.BatteryThresholds;
import dev.nez.analytics.data.power.PowerConsumptionDeserializer;
import dev.nez.analytics.data.power.PowerThresholds;
import dev.nez.analytics.data.power.PowerConsumptionSerializer;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class PowerConsumptionStream {

    @ConfigProperty(name = "kafka.topic.power.events")
    String consumptionTopic;

    @ConfigProperty(name = "kafka.topic.power.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    PowerConsumptionAnalyzer analyzer;

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();

        final var alertSerde = Serdes.serdeFrom(
            new AlertSerializer(),
            new AlertDeserializer()
        );

        final var consumptionSerde = Serdes.serdeFrom(
            new PowerConsumptionSerializer(),
            new PowerConsumptionDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(PowerThresholds.class)
        );

        final KTable<Long, PowerThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde)
        );

        final KStream<Long, PowerConsumptionData> consumptionStream = builder.stream(
            consumptionTopic,
            Consumed.with(longSerde, consumptionSerde)
        );

        consumptionStream
            .leftJoin(
                thresholdsTable,
                (consumptionEvent, latestThreshold) -> {
                    if (latestThreshold == null) {
                        return null;
                    }

                    return analyzer.checkThreshold(consumptionEvent, latestThreshold);
                },
                Joined.with(longSerde, consumptionSerde, thresholdsSerde)
            )
            .filter((_, alertMessage) -> alertMessage != null)
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}

