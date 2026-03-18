package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient;
import dev.nez.producer.simulation.generator.AirDataGenerator;
import dev.nez.producer.simulation.generator.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.PowerDataGenerator;
import dev.nez.producer.simulation.generator.SmokeDataGenerator;

import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;

@ApplicationScoped
public class Simulator {

    @Inject
    ProducerClient producerClient;

    @ConfigProperty(name = "gen.air.proto", defaultValue = "0")
    Integer numAirQualityDevicesProto;

    @ConfigProperty(name = "gen.air.json", defaultValue = "0")
    Integer numAirQualityDevicesJson;

    @ConfigProperty(name = "gen.power.proto", defaultValue = "0")
    Integer numPowerConsumptionDevicesProto;

    @ConfigProperty(name = "gen.smoke.proto", defaultValue = "0")
    Integer numSmokeDetectorProto;

    void onStart(@Observes StartupEvent ev) {
        final var devices = initDataGenerators();

        for (var generator  : devices) {
            producerClient.startSimulation(generator);
        }
    }

    private ArrayList<DeviceDataGenerator> initDataGenerators() {
        final var generators = new ArrayList<DeviceDataGenerator>(1000);

        int deviceId = 1;

        for (int i = 0; i < numAirQualityDevicesProto; i++) {
            generators.add(new AirDataGenerator(
                "hardware" + deviceId++,
                "pass",
                "dev/air/p",
                "dev/batt/pow",
                DeviceDataGenerator.MessageType.PROTO
            ));
        }

        for (int i = 0; i < numAirQualityDevicesJson; i++) {
            generators.add(new AirDataGenerator(
                "hardware" + deviceId++,
                "pass",
                "dev/air/j",
                "dev/batt/pow",
                DeviceDataGenerator.MessageType.JSON
            ));
        }

        for (int i = 0; i < numPowerConsumptionDevicesProto; i++) {
            generators.add(new PowerDataGenerator(
                "hardware" + deviceId++,
                "pass",
                "dev/power/p",
                DeviceDataGenerator.MessageType.PROTO
            ));
        }

        for (int i = 0; i < numSmokeDetectorProto; i++) {
            generators.add(new SmokeDataGenerator(
                "hardware" + deviceId++,
                "pass",
                "dev/smoke/p",
                "dev/batt/pow",
                DeviceDataGenerator.MessageType.PROTO
            ));
        }

        return generators;
    }
}