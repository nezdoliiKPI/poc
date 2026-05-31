package dev.nez.producer.simulation;

import dev.nez.producer.client.ProducerClient.DeviceSession;
import dev.nez.producer.dto.rest.ProducerConfig;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class Simulations {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<DeviceSession>> simulations = new ConcurrentHashMap<>();
    private final SimulationConfig staticConfig;

    @Inject
    Simulations(SimulationConfig config) {
        this.staticConfig = config;

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

    public Uni<ProducerConfig> getConfig() {
        final var currentConfig = new ProducerConfig(
            (int)getSessionList(staticConfig.air().json().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.air().proto().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.power().json().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.power().proto().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.smoke().json().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.smoke().proto().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.temp().json().topic()).stream().filter(DeviceSession::isRunning).count(),
            (int)getSessionList(staticConfig.temp().proto().topic()).stream().filter(DeviceSession::isRunning).count(),
            getIntensity()
        );

        return Uni.createFrom().item(currentConfig);
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
