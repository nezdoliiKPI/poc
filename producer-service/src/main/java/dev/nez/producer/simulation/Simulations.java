package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient.DeviceSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class Simulations {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<DeviceSession>> simulations = new ConcurrentHashMap<>();

    @Inject
    Simulations(SimulationConfig config) {
        // AIR
        simulations.put(config.air().proto().topic(), new CopyOnWriteArrayList<>());
        simulations.put(config.air().json().topic(), new CopyOnWriteArrayList<>());

        // POWER
        simulations.put(config.power().proto().topic(), new CopyOnWriteArrayList<>());
        simulations.put(config.power().json().topic(), new CopyOnWriteArrayList<>());

        // SMOKE
        simulations.put(config.smoke().proto().topic(), new CopyOnWriteArrayList<>());
        simulations.put(config.smoke().json().topic(), new CopyOnWriteArrayList<>());

        // Temperature
        simulations.put(config.temp().proto().topic(), new CopyOnWriteArrayList<>());
        simulations.put(config.temp().json().topic(), new CopyOnWriteArrayList<>());
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

    public void runAll() {
        for (var sessionList : simulations.values()) {
            for (var deviceSession : sessionList) {
                deviceSession.run();
            }
        }
    }

    public List<Long> getSessionIds(String topic) {
        final var arr = Objects.requireNonNull(simulations.get(topic), "Error: unknown topic");
        return arr.stream().map(DeviceSession::getDeviceId).toList();
    }

    public CopyOnWriteArrayList<DeviceSession> getSessionList(String topic) {
        return Objects.requireNonNull(simulations.get(topic), "Error: unknown topic");
    }
}
