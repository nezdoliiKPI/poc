package dev.nez.producer.simulation.generator;

import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.generator.data.AirDataGenerator;
import dev.nez.producer.simulation.generator.data.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.data.PowerDataGenerator;
import dev.nez.producer.simulation.generator.data.SmokeDataGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ApplicationScoped
public class GeneratorFactory {
    private final AtomicInteger deviceId = new AtomicInteger(0);
    private final Map<String, Supplier<DeviceDataGenerator>> suppliers = new HashMap<>();

    public Supplier<DeviceDataGenerator> getSupplier(String topic) {
        var gen = suppliers.get(topic);
        if (gen == null) {
            throw new IllegalArgumentException("No supplier for topic " + topic);
        }
        return gen;
    }

    @Inject
    public GeneratorFactory(SimulationConfig config) {
        final String batteryTopicProto = config.battery().proto().topic();
        final String batteryTopicJson = config.battery().json().topic();

        // AIR
        suppliers.put(config.air().proto().topic(), () -> new AirDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.air().proto().topic(),
            batteryTopicProto,
            DeviceDataGenerator.MessageType.PROTO
        ));

        suppliers.put(config.air().json().topic(), () -> new AirDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.air().json().topic(),
            batteryTopicJson,
            DeviceDataGenerator.MessageType.JSON
        ));

        // POWER
        suppliers.put(config.power().proto().topic(), () -> new PowerDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.power().proto().topic(),
            DeviceDataGenerator.MessageType.PROTO
        ));

        suppliers.put(config.power().json().topic(), () -> new PowerDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.power().json().topic(),
            DeviceDataGenerator.MessageType.JSON
        ));

        // SMOKE
        suppliers.put(config.smoke().proto().topic(), () -> new SmokeDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.smoke().proto().topic(),
            batteryTopicProto,
            DeviceDataGenerator.MessageType.PROTO
        ));

        suppliers.put(config.smoke().json().topic(), () -> new SmokeDataGenerator(
            "hardware" + deviceId.incrementAndGet(),
            "pass",
            config.smoke().json().topic(),
            batteryTopicJson,
            DeviceDataGenerator.MessageType.JSON
        ));
    }
}
