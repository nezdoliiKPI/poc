package dev.nez.producer.simulation.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class DynamicConfig {

    @Inject
    Event<ConfigChangeEvent> notifier;

    private final SimulationConfig baseConfig;

    private final AtomicInteger airJsonCount;
    private final AtomicInteger airProtoCount;

    private final AtomicInteger powerJsonCount;
    private final AtomicInteger powerProtoCount;

    private final AtomicInteger smokeJsonCount;
    private final AtomicInteger smokeProtoCount;

    @Inject
    public DynamicConfig(SimulationConfig config) {
        baseConfig = config;

        this.airJsonCount = new AtomicInteger(baseConfig.air().json().count());
        this.airProtoCount = new AtomicInteger(baseConfig.air().proto().count());

        this.powerJsonCount = new AtomicInteger(baseConfig.power().json().count());
        this.powerProtoCount = new AtomicInteger(baseConfig.power().proto().count());

        this.smokeJsonCount = new AtomicInteger(baseConfig.smoke().json().count());
        this.smokeProtoCount = new AtomicInteger(baseConfig.smoke().proto().count());
    }

    public record ConfigChangeEvent(
        String topic,
        int newCount
    ) {}

    // ================= AIR =================

    public String getAirJsonTopic() {
        return baseConfig.air().json().topic();
    }
    public Integer getAirJsonCount() {
        return airJsonCount.get();
    }

    public void setAirJsonCount(int newCount) {
        if (airJsonCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getAirJsonTopic(), newCount));
        }
    }

    public String getAirProtoTopic() {
        return baseConfig.air().proto().topic();
    }
    public Integer getAirProtoCount() {
        return airProtoCount.get();
    }

    public void setAirProtoCount(int newCount) {
        if (airProtoCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getAirProtoTopic(), newCount));
        }
    }

    // ================= POWER =================

    public String getPowerJsonTopic() {
        return baseConfig.power().json().topic();
    }
    public Integer getPowerJsonCount() {
        return powerJsonCount.get();
    }

    public void setPowerJsonCount(int newCount) {
        if (powerJsonCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getPowerJsonTopic(), newCount));
        }
    }

    public String getPowerProtoTopic() {
        return baseConfig.power().proto().topic();
    }
    public Integer getPowerProtoCount() {
        return powerProtoCount.get();
    }

    public void setPowerProtoCount(int newCount) {
        if (powerProtoCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getPowerProtoTopic(), newCount));
        }
    }

    // ================= SMOKE =================

    public String getSmokeJsonTopic() {
        return baseConfig.smoke().json().topic();
    }
    public Integer getSmokeJsonCount() {
        return smokeJsonCount.get();
    }

    public void setSmokeJsonCount(int newCount) {
        if (smokeJsonCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getSmokeJsonTopic(), newCount));
        }
    }

    public String getSmokeProtoTopic() {
        return baseConfig.smoke().proto().topic();
    }
    public Integer getSmokeProtoCount() {
        return smokeProtoCount.get();
    }

    public void setSmokeProtoCount(int newCount) {
        if (smokeProtoCount.getAndSet(newCount) != newCount) {
            notifier.fire(new ConfigChangeEvent(getSmokeProtoTopic(), newCount));
        }
    }

    // ================= BATTERY =================

    public String getBatteryJsonTopic() {
        return baseConfig.battery().json().topic();
    }

    public String getBatteryProtoTopic() {
        return baseConfig.battery().proto().topic();
    }
}
