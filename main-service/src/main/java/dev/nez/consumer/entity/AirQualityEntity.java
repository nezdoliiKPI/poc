package dev.nez.consumer.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@RegisterForReflection
@Entity
@Table(name = "air_quality")
public class AirQualityEntity extends PanacheEntity implements Timed {

    @Column(name = "device_id",nullable = false)
    public Long deviceId;

    public Integer co2;

    public Float pm25;

    public Float pm10;

    public Float tvoc;

    public Float temperature;

    public Float humidity;

    @Column(name = "time_date", nullable = false)
    public Instant timestamp;

    public AirQualityEntity() {}

    public AirQualityEntity(
        Long deviceId,
        Integer co2,
        Float pm25,
        Float pm10,
        Float tvoc,
        Float temperature,
        Float humidity,
        Instant timestamp
    ) {
        this.deviceId = deviceId;
        this.co2 = co2;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.tvoc = tvoc;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }
}
