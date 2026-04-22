package dev.nez.producer.simulation.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class DynamicConfig {
    private final SimulationConfig baseConfig;

    private final AtomicInteger airJsonCount;
    private final AtomicInteger airProtoCount;

    private final AtomicInteger powerJsonCount;
    private final AtomicInteger powerProtoCount;

    private final AtomicInteger smokeJsonCount;
    private final AtomicInteger smokeProtoCount;

    private final AtomicInteger batteryJsonCount;
    private final AtomicInteger batteryProtoCount;

    @Inject
    public DynamicConfig(SimulationConfig config) {
        baseConfig = config;

        this.airJsonCount = new AtomicInteger(baseConfig.air().json().count());
        this.airProtoCount = new AtomicInteger(baseConfig.air().proto().count());

        this.powerJsonCount = new AtomicInteger(baseConfig.power().json().count());
        this.powerProtoCount = new AtomicInteger(baseConfig.power().proto().count());

        this.smokeJsonCount = new AtomicInteger(baseConfig.smoke().json().count());
        this.smokeProtoCount = new AtomicInteger(baseConfig.smoke().proto().count());

        this.batteryJsonCount = new AtomicInteger(baseConfig.battery().json().count());
        this.batteryProtoCount = new AtomicInteger(baseConfig.battery().proto().count());
    }

    // ================= AIR =================

    public String getAirJsonTopic() {
        return baseConfig.air().json().topic();
    }

    public Integer getAirJsonCount() {
        return airJsonCount.get();
    }

    public String getAirProtoTopic() {
        return baseConfig.air().proto().topic();
    }

    public Integer getAirProtoCount() {
        return airProtoCount.get();
    }

    // ================= POWER =================

    public String getPowerJsonTopic() {
        return baseConfig.power().json().topic();
    }

    public Integer getPowerJsonCount() {
        return powerJsonCount.get();
    }

    public String getPowerProtoTopic() {
        return baseConfig.power().proto().topic();
    }

    public Integer getPowerProtoCount() {
        return powerProtoCount.get();
    }

    // ================= SMOKE =================

    public String getSmokeJsonTopic() {
        return baseConfig.smoke().json().topic();
    }

    public Integer getSmokeJsonCount() {
        return smokeJsonCount.get();
    }

    public String getSmokeProtoTopic() {
        return baseConfig.smoke().proto().topic();
    }

    public Integer getSmokeProtoCount() {
        return smokeProtoCount.get();
    }

    // ================= BATTERY =================

    public String getBatteryJsonTopic() {
        return baseConfig.battery().json().topic();
    }

    public Integer getBatteryJsonCount() {
        return batteryJsonCount.get();
    }

    public String getBatteryProtoTopic() {
        return baseConfig.battery().proto().topic();
    }

    public Integer getBatteryProtoCount() {
        return batteryProtoCount.get();
    }
}
