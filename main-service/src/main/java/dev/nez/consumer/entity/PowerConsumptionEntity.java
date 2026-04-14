package dev.nez.consumer.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "power_consumption")
public class PowerConsumptionEntity extends PanacheEntity {

    @Column(name = "device_id", nullable = false)
    public Long deviceId;

    public Float voltage;

    public Float current;

    public Float power;

    @Column(name = "time_date", nullable = false)
    public Instant timestamp;

    public PowerConsumptionEntity() {}

    public PowerConsumptionEntity(
        Long deviceId,
        Float voltage,
        Float current,
        Float power,
        Instant timestamp
    ) {
        this.deviceId = deviceId;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.timestamp = timestamp;
    }
}
