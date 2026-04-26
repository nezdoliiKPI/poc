package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient;
import dev.nez.producer.client.ProducerClient.DeviceSession;

import dev.nez.producer.simulation.generator.GeneratorFactory;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class Simulator {
    public static final String CONFIG_ADDRESS = "config-change-event";
    private final Map<String, ArrayList<DeviceSession>> simulations = new HashMap<>();

    @Inject
    ProducerClient client;

    @Inject
    GeneratorFactory factory;

    public record ConfigChangeEvent(
        String topic,
        int newCount
    ) {}

    void onStart(@Observes StartupEvent ev, SimulationConfig config) {
        // AIR
        simulations.put(config.air().proto().topic(), new ArrayList<>());
        simulations.put(config.air().json().topic(), new ArrayList<>());

        addGenerators(config.air().proto().topic(), config.air().proto().count());
        addGenerators(config.air().json().topic(), config.air().json().count());

        // POWER
        simulations.put(config.power().proto().topic(), new ArrayList<>());
        simulations.put(config.power().json().topic(), new ArrayList<>());

        addGenerators(config.power().proto().topic(), config.power().proto().count());
        addGenerators(config.power().json().topic(), config.power().json().count());

        // SMOKE
        simulations.put(config.smoke().proto().topic(), new ArrayList<>());
        simulations.put(config.smoke().json().topic(), new ArrayList<>());

        addGenerators(config.smoke().proto().topic(), config.smoke().proto().count());
        addGenerators(config.smoke().json().topic(), config.smoke().json().count());

        simulations.values().stream()
            .flatMap(ArrayList::stream)
            .forEach(DeviceSession::run);

        Log.info("Predicted messages per second: " + getIntensity());
    }

    @ConsumeEvent(CONFIG_ADDRESS)
    Float onConfigChange(ConfigChangeEvent event) {
        Log.info("Configuration set, topic: " + event.topic() + ", count: " + event.newCount());

        final var sessions = Objects.requireNonNull(
            simulations.get(event.topic()), "Not found topic: " + event.topic()
        );

        final int newCount = event.newCount();

        if (sessions.size() < newCount) {
            addGenerators(event.topic(), newCount - sessions.size());
        }

        for (int i = 0; i < sessions.size(); i++) {
            var session = sessions.get(i);
            if (i < newCount) session.run(); else session.stop();
        }

        final float intensity = getIntensity();

        Log.info("Predicted messages per second: " + intensity);
        return intensity;
    }

    public float getIntensity() {
        float totalIntensity = 0.0f;

        for (var sessionList : simulations.values()) {
            for (var deviceSession : sessionList) {
                if (deviceSession.isRunning()) {
                    totalIntensity += deviceSession.getIntensityPerSecond();
                }
            }
        }

        return totalIntensity;
    }

    private void addGenerators(String topic, int count) {
        var factory = this.factory.getSupplier(topic);
        var arr = simulations.get(topic);

        for (int i = 0; i < count; i++) {
            arr.add(client.createSession(factory.get()));
        }
    }
}