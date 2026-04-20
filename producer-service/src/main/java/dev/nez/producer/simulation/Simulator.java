package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient;
import dev.nez.producer.simulation.generator.AirDataGenerator;
import dev.nez.producer.simulation.generator.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.PowerDataGenerator;
import dev.nez.producer.simulation.generator.SmokeDataGenerator;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ApplicationScoped
public class Simulator {

    @Inject
    SimulationConfig config;

    @Inject
    ProducerClient producerClient;

    void onStart(@Observes StartupEvent ev) {
        final var devices = initDataGenerators();

        final float intensity = devices.stream()
                .map(DeviceDataGenerator::getIntensityPerSecond)
                .reduce(0.0f, Float::sum);

        Log.info("Predicted messages per second: " + intensity);

        for (var generator  : devices) {
            producerClient.startSimulation(generator);
        }
    }

    private ArrayList<DeviceDataGenerator> initDataGenerators() {
        final var generators = new ArrayList<DeviceDataGenerator>(1000);

        final String batteryTopicProto = config.battery().proto().topic();
        final String batteryTopicJson = config.battery().json().topic();

        final var deviceId = new AtomicInteger(0);

        // AIR
        addGenerators(generators, config.air().proto().count(), () -> new AirDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.air().proto().topic(),
            batteryTopicProto,
            DeviceDataGenerator.MessageType.PROTO
        ));

        addGenerators(generators, config.air().json().count(), () -> new AirDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.air().json().topic(),
            batteryTopicJson,
            DeviceDataGenerator.MessageType.JSON
        ));

        // POWER
        addGenerators(generators, config.power().proto().count(), () -> new PowerDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.power().proto().topic(),
            DeviceDataGenerator.MessageType.PROTO
        ));

        addGenerators(generators, config.power().json().count(), () -> new PowerDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.power().json().topic(),
            DeviceDataGenerator.MessageType.JSON
        ));

        // SMOKE
        addGenerators(generators, config.smoke().proto().count(), () -> new SmokeDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.smoke().proto().topic(),
            batteryTopicProto,
            DeviceDataGenerator.MessageType.PROTO
        ));

        addGenerators(generators, config.smoke().json().count(), () -> new SmokeDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.smoke().json().topic(),
            batteryTopicJson,
            DeviceDataGenerator.MessageType.JSON
        ));

        return generators;
    }

    private void addGenerators(
        List<DeviceDataGenerator> list,
        int count,
        Supplier<DeviceDataGenerator> factory
    ) {
        for (int i = 0; i < count; i++) {
            list.add(factory.get());
        }
    }
}