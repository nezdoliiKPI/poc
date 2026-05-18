package dev.nez.analytics.topology.stream;

import dev.nez.analytics.analyzer.TemperatureAnalyzer;
import dev.nez.analytics.data.JsonDeserializer;
import dev.nez.analytics.data.JsonSerializer;
import dev.nez.analytics.data.ProtobufSerializer;
import dev.nez.analytics.data.temperature.*;

import dev.nez.analytics.data.temperature.TemperatureDeserializer;
import dev.nez.dto.proto.timeddata.TemperatureData;

import dev.nez.notification.Alert;
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

        final var alertSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(Alert.class)
        );
        final var tempSerde = Serdes.serdeFrom(
            new ProtobufSerializer<>(),
            new TemperatureDeserializer()
        );
        final var thresholdsSerde = Serdes.serdeFrom(
            new JsonSerializer<>(),
            new JsonDeserializer<>(TemperatureThresholds.class)
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
            .join(
                thresholdsTable,
                (event, latestThreshold) -> {
                    return analyzer.checkThreshold(event, latestThreshold);
                },
                Joined.with(longSerde, tempSerde, thresholdsSerde)
            )
            .filter((_, alertMessage) -> alertMessage != null)
            .to(notificationsTopic, Produced.with(longSerde, alertSerde));
    }
}
