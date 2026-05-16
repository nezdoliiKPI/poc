package dev.nez.analytics.stream;

import dev.nez.analytics.analyzer.PowerConsumptionAnalyzer;
import dev.nez.analytics.data.deserializer.PowerConsumptionDeserializer;
import dev.nez.analytics.data.deserializer.PowerThresholdsDeserializer;
import dev.nez.analytics.data.event.PowerThresholds;
import dev.nez.analytics.data.serializer.PowerConsumptionSerializer;
import dev.nez.analytics.data.serializer.PowerThresholdsSerializer;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class PowerConsumptionStream {

    @ConfigProperty(name = "kafka.topic.consumption.events")
    String consumptionTopic;

    @ConfigProperty(name = "kafka.topic.power.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    PowerConsumptionAnalyzer analyzer;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        final var longSerde = Serdes.Long();
        final var stringSerde = Serdes.String();

        final var consumptionSerde = Serdes.serdeFrom(
            new PowerConsumptionSerializer(),
            new PowerConsumptionDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new PowerThresholdsSerializer(),
            new PowerThresholdsDeserializer()
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
            .to(notificationsTopic, Produced.with(longSerde, stringSerde));

        return builder.build();
    }
}

