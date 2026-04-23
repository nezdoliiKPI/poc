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

    private Integer airJsonCount;
    private Integer airProtoCount;

    private Integer powerJsonCount;
    private Integer powerProtoCount;

    private Integer smokeJsonCount;
    private Integer smokeProtoCount;

    @Inject
    public DynamicConfig(SimulationConfig config) {
        baseConfig = config;

        this.airJsonCount = baseConfig.air().json().count();
        this.airProtoCount = baseConfig.air().proto().count();

        this.powerJsonCount = baseConfig.power().json().count();
        this.powerProtoCount = baseConfig.power().proto().count();

        this.smokeJsonCount = baseConfig.smoke().json().count();
        this.smokeProtoCount = baseConfig.smoke().proto().count();
    }

    public record ConfigChangeEvent(
        String topic,
        int newCount
    ) {}

    // ================= AIR =================

    public String getAirJsonTopic() {
        return baseConfig.air().json().topic();
    }
    public int getAirJsonCount() {
        return airJsonCount;
    }

    public void setAirJsonCount(int newCount) {
        if (airJsonCount != newCount) {
            airJsonCount = newCount;
            notifier.fire(new ConfigChangeEvent(getAirJsonTopic(), newCount));
        }
    }

    public String getAirProtoTopic() { return baseConfig.air().proto().topic();}
    public int getAirProtoCount() {return airProtoCount;}

    public void setAirProtoCount(int newCount) {
        if (airProtoCount != newCount) {
            airProtoCount = newCount;
            notifier.fire(new ConfigChangeEvent(getAirProtoTopic(), newCount));
        }
    }

    // ================= POWER =================

    public String getPowerJsonTopic() {
        return baseConfig.power().json().topic();
    }
    public int getPowerJsonCount() {
        return powerJsonCount;
    }

    public void setPowerJsonCount(int newCount) {
        if (powerJsonCount != newCount) {
            powerJsonCount = newCount;
            notifier.fire(new ConfigChangeEvent(getPowerJsonTopic(), newCount));
        }
    }

    public String getPowerProtoTopic() {
        return baseConfig.power().proto().topic();
    }
    public int getPowerProtoCount() {
        return powerProtoCount;
    }

    public void setPowerProtoCount(int newCount) {
        if (powerProtoCount != newCount) {
            powerProtoCount = newCount;
            notifier.fire(new ConfigChangeEvent(getPowerProtoTopic(), newCount));
        }
    }

    // ================= SMOKE =================

    public String getSmokeJsonTopic() {
        return baseConfig.smoke().json().topic();
    }
    public int getSmokeJsonCount() {
        return smokeJsonCount;
    }

    public void setSmokeJsonCount(int newCount) {
        if (smokeJsonCount != newCount) {
            smokeJsonCount = newCount;
            notifier.fire(new ConfigChangeEvent(getSmokeJsonTopic(), newCount));
        }
    }

    public String getSmokeProtoTopic() {
        return baseConfig.smoke().proto().topic();
    }
    public int getSmokeProtoCount() {
        return smokeProtoCount;
    }

    public void setSmokeProtoCount(int newCount) {
        if (smokeProtoCount != newCount) {
            smokeProtoCount = newCount;
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
