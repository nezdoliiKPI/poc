package dev.nez.analytics.topology;

import dev.nez.analytics.topology.stream.*;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

@Singleton
public class TopologyProvider {

    @Inject
    AirQualityStream airQualityStream;

    @Inject
    SmokeDetectorStream smokeDetectorStream;

    @Inject
    BatteryStream batteryStream;

    @Inject
    TemperatureStream temperatureStream;

    @Inject
    PowerConsumptionStream powerConsumptionStream;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        airQualityStream.addTopology(builder);
        smokeDetectorStream.addTopology(builder);
        batteryStream.addTopology(builder);
        temperatureStream.addTopology(builder);
        powerConsumptionStream.addTopology(builder);

        return builder.build();
    }
}
