package dev.nez.analytics.topology.stream;
import dev.nez.alert.AlertDeserializer;
import dev.nez.alert.AlertSerializer;
import dev.nez.analytics.analyzer.AirQualityAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.air.*;

import dev.nez.dto.proto.timeddata.AirQualityData;

import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class AirQualityStream {

    @ConfigProperty(name = "kafka.topic.air.events")
    String airQualityTopic;

    @ConfigProperty(name = "kafka.topic.air.thresholds")
    String thresholdsTopic;

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    AirQualityAnalyzer analyzer;

    public void addTopology(StreamsBuilder builder) {
        final var longSerde = Serdes.Long();
        final var alertSerde = Serdes.serdeFrom(new AlertSerializer(), new AlertDeserializer());

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
            .leftJoin(
                thresholdsTable,
                (event, latestThreshold) -> {
                    if (latestThreshold == null) {
                        return null;
                    }
                    return analyzer.checkThreshold(event, latestThreshold);
                },
                Joined.with(longSerde, airQualitySerde, thresholdsSerde)
            )
            .filter((_, alertMessage) -> alertMessage != null)
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}
