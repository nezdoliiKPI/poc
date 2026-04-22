package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient;
import dev.nez.producer.client.ProducerClient.DeviceSession;
import dev.nez.producer.simulation.config.DynamicConfig;
import dev.nez.producer.simulation.generator.AirDataGenerator;
import dev.nez.producer.simulation.generator.DeviceDataGenerator;
import dev.nez.producer.simulation.generator.PowerDataGenerator;
import dev.nez.producer.simulation.generator.SmokeDataGenerator;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ApplicationScoped
public class Simulator {
    private final DynamicConfig config;
    private final ProducerClient client;
    private final Map<String, ArrayList<DeviceSession>> simulations = new HashMap<>();
    private final GeneratorFactory creator;

    @Inject
    public Simulator(DynamicConfig config, ProducerClient client) {
        this.config = config;
        this.client = client;
        this.creator = new GeneratorFactory();
    }

    void onStart(@Observes StartupEvent ev) {
        initSimulations();

        Log.info("Predicted messages per second: " + getIntensity());

        simulations.values().stream()
            .flatMap(ArrayList::stream)
            .forEach(DeviceSession::run);
    }

    private float getIntensity() {
        return simulations.values().stream()
            .flatMap(ArrayList::stream)
            .map(DeviceSession::getIntensityPerSecond)
            .reduce(0.0f, Float::sum);
    }

    private void initSimulations() {
        // AIR
        simulations.put(config.getAirProtoTopic(), new ArrayList<>());
        simulations.put(config.getAirJsonTopic(), new ArrayList<>());

        addGenerators(config.getAirProtoTopic(), config.getAirProtoCount());
        addGenerators(config.getAirJsonTopic(), config.getAirJsonCount());

        // POWER
        simulations.put(config.getPowerProtoTopic(), new ArrayList<>());
        simulations.put(config.getPowerJsonTopic(), new ArrayList<>());

        addGenerators(config.getPowerProtoTopic(), config.getPowerProtoCount());
        addGenerators(config.getPowerJsonTopic(), config.getPowerJsonCount());

        // SMOKE
        simulations.put(config.getSmokeProtoTopic(), new ArrayList<>());
        simulations.put(config.getSmokeJsonTopic(), new ArrayList<>());

        addGenerators(config.getSmokeProtoTopic(), config.getSmokeProtoCount());
        addGenerators(config.getSmokeJsonTopic(), config.getSmokeJsonCount());
    }

    private void addGenerators(String topic, int count) {
        var factory = creator.getSupplier(topic);
        var arr = simulations.get(topic);

        for (int i = 0; i < count; i++) {
            arr.add(client.createSession(factory.get()));
        }
    }

    private class GeneratorFactory {
        private final AtomicInteger deviceId = new AtomicInteger(0);
        private final Map<String, Supplier<DeviceDataGenerator>> suppliers = new HashMap<>();

        public Supplier<DeviceDataGenerator> getSupplier(String topic) {
            var gen = suppliers.get(topic);
            if (gen == null) {
                throw new IllegalArgumentException("No supplier for topic " + topic);
            }
            return gen;
        }

        public GeneratorFactory() {
            final String batteryTopicProto = config.getBatteryProtoTopic();
            final String batteryTopicJson = config.getBatteryJsonTopic();

            // AIR
            suppliers.put(config.getAirProtoTopic(), () -> new AirDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getAirProtoTopic(),
                batteryTopicProto,
                DeviceDataGenerator.MessageType.PROTO
            ));

            suppliers.put(config.getAirJsonTopic(), () -> new AirDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getAirJsonTopic(),
                batteryTopicJson,
                DeviceDataGenerator.MessageType.JSON
            ));

            // POWER
            suppliers.put(config.getPowerProtoTopic(), () -> new PowerDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getPowerProtoTopic(),
                DeviceDataGenerator.MessageType.PROTO
            ));

            suppliers.put(config.getPowerJsonTopic(), () -> new PowerDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getPowerJsonTopic(),
                DeviceDataGenerator.MessageType.JSON
            ));

            // SMOKE
            suppliers.put(config.getSmokeProtoTopic(), () -> new SmokeDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getSmokeProtoTopic(),
                batteryTopicProto,
                DeviceDataGenerator.MessageType.PROTO
            ));

            suppliers.put(config.getSmokeJsonTopic(), () -> new SmokeDataGenerator(
                "hardware" + deviceId.incrementAndGet(),
                "pass",
                config.getSmokeJsonTopic(),
                batteryTopicJson,
                DeviceDataGenerator.MessageType.JSON
            ));
        }
    }
}