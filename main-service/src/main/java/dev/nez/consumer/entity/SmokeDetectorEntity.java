package dev.nez.consumer.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "smoke_detector")
public class SmokeDetectorEntity extends PanacheEntity {

    @Column(name = "device_id",nullable = false)
    public Long deviceId;

    @Column(name = "smoke_raw")
    public Integer smokeRaw;

    @Column(name = "co_level")
    public Integer coLevel;

    @Column(name = "time_date", nullable = false)
    public Instant timestamp;

    public SmokeDetectorEntity() {}

    public SmokeDetectorEntity(
        Long deviceId,
        Integer smokeRaw,
        Integer coLevel,
        Instant timestamp
    ) {
        this.deviceId = deviceId;
        this.smokeRaw = smokeRaw;
        this.coLevel = coLevel;
        this.timestamp = timestamp;
    }
}
