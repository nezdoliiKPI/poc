package dev.nez.consumer.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "battery_data")
public class BatteryDataEntity extends PanacheEntity {

    @Column(name = "device_id", nullable = false)
    public Long deviceId;

    public Float val;

    @Column(name = "time_date", nullable = false)
    public Instant timestamp;

    public BatteryDataEntity() {}

    public BatteryDataEntity(
        Long deviceId,
        Float val,
        Instant timestamp
    ) {
        this.deviceId = deviceId;
        this.val = val;
        this.timestamp = timestamp;
    }
}
