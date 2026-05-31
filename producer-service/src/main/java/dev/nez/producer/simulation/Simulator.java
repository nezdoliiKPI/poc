package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient;
import dev.nez.producer.client.ProducerClient.DeviceSession;

import dev.nez.producer.dto.rest.ProducerConfig;
import dev.nez.producer.simulation.generator.GeneratorFactory;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Objects;

@ApplicationScoped
public class Simulator {

    @Inject
    Simulations simulations;

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
        addGenerators(config.air().proto().topic(), config.air().proto().count());
        addGenerators(config.air().json().topic(), config.air().json().count());

        // POWER
        addGenerators(config.power().proto().topic(), config.power().proto().count());
        addGenerators(config.power().json().topic(), config.power().json().count());

        // SMOKE
        addGenerators(config.smoke().proto().topic(), config.smoke().proto().count());
        addGenerators(config.smoke().json().topic(), config.smoke().json().count());

        // Temperature
        addGenerators(config.temp().proto().topic(), config.temp().proto().count());
        addGenerators(config.temp().json().topic(), config.temp().json().count());

        simulations.runAll();
        Log.info("Predicted messages per second: " + simulations.getIntensity());
    }

    public Uni<ProducerConfig> getConfig() {
        return simulations.getConfig();
    }

    public Uni<Float> configChange(ConfigChangeEvent event) {
        Log.info("Configuration set, topic: " + event.topic() + ", count: " + event.newCount());

        final var sessions = Objects.requireNonNull(simulations.getSessionList(event.topic()), "Not found topic: " + event.topic());
        final int newCount = event.newCount();

        if (sessions.size() < newCount) {
            addGenerators(event.topic(), newCount - sessions.size());
        }

        for (int i = 0; i < sessions.size(); i++) {
            var session = sessions.get(i);

            if (i < newCount) {
                session.run();
            } else {
                session.stop();
            }
        }

        return Uni.createFrom().item(simulations.getIntensity())
            .invoke(intensity -> Log.info("Predicted messages per second: " + intensity));
    }

    private void addGenerators(String topic, int count) {
        var factory = this.factory.getSupplier(topic);
        var arr = new ArrayList<DeviceSession>(count);

        for (int i = 0; i < count; i++) {
            arr.add(client.createSession(factory.get()));
        }

        simulations.getSessionList(topic).addAll(arr);
    }
}