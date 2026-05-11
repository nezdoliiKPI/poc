package dev.nez.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@RegisterForReflection
@Entity
@Table(name = "power_thresholds")
public class PowerThresholds extends PanacheEntityBase {
    @Id
    @Column(name = "device_id")
    public Long deviceId;

    @Column(name = "min_voltage")
    public Float minVoltage;

    @Column(name = "max_voltage")
    public Float maxVoltage;

    @Column(name = "max_current")
    public Float maxCurrent;

    @Column(name = "max_power")
    public Float maxPower;

    public PowerThresholds() {}

    public PowerThresholds(
        Long deviceId,
        Float minVoltage,
        Float maxVoltage,
        Float maxCurrent,
        Float maxPower
    ) {
        this.deviceId = deviceId;
        this.maxCurrent = maxCurrent;
        this.minVoltage = minVoltage;
        this.maxVoltage = maxVoltage;
        this.maxPower = maxPower;
    }
}
