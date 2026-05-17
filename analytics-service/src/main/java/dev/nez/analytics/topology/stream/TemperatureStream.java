package dev.nez.analytics.topology.stream;

import dev.nez.alert.AlertDeserializer;
import dev.nez.alert.AlertSerializer;
import dev.nez.analytics.analyzer.TemperatureAnalyzer;
import dev.nez.analytics.data.temperature.*;

import dev.nez.analytics.data.temperature.TemperatureDeserializer;
import dev.nez.analytics.data.temperature.TemperatureSerializer;
import dev.nez.dto.proto.timeddata.TemperatureData;

import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class TemperatureStream {

    @ConfigProperty(name = "kafka.topic.temp.events")
    String tempTopic;

    @ConfigProperty(name = "kafka.topic.temp.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    TemperatureAnalyzer analyzer;

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();
        final var alertSerde = Serdes.serdeFrom(new AlertSerializer(), new AlertDeserializer());

        final var tempSerde = Serdes.serdeFrom(
            new TemperatureSerializer(),
            new TemperatureDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new TemperatureThresholdsSerializer(),
            new TemperatureThresholdsDeserializer()
        );

        final KTable<Long, TemperatureThresholds> thresholdsTable = builder.table(
            thresholdsTopic,
            Consumed.with(longSerde, thresholdsSerde)
        );

        final KStream<Long, TemperatureData> tempStream = builder.stream(
            tempTopic,
            Consumed.with(longSerde, tempSerde)
        );

        tempStream
            .leftJoin(
                thresholdsTable,
                (event, latestThreshold) -> {
                    if (latestThreshold == null) {
                        return null;
                    }
                    return analyzer.checkThreshold(event, latestThreshold);
                },
                Joined.with(longSerde, tempSerde, thresholdsSerde)
            )
            .filter((_, alertMessage) -> alertMessage != null)
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}
