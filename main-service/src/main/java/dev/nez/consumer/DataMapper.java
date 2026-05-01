package dev.nez.consumer;
import dev.nez.consumer.entity.AirQualityEntity;
import dev.nez.consumer.entity.BatteryDataEntity;
import dev.nez.consumer.entity.PowerConsumptionEntity;
import dev.nez.consumer.entity.SmokeDetectorEntity;
import dev.nez.dto.proto.timeddata.AirQualityData;
import dev.nez.dto.proto.timeddata.BatteryData;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import jakarta.inject.Singleton;

import io.vertx.mutiny.sqlclient.Tuple;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Singleton
public class DataMapper {
    public PowerConsumptionEntity toEntity(PowerConsumptionData proto) {
        return new PowerConsumptionEntity(
            proto.getDeviceId(),
            proto.getVoltage(),
            proto.getCurrent(),
            proto.getPower(),
            toInstant(proto.getTimestamp())
        );
    }

    public Tuple toTuple(PowerConsumptionEntity entity) {
        return Tuple.of(
            entity.deviceId,
            entity.voltage,
            entity.current,
            entity.power,
            entity.timestamp.atOffset(ZoneOffset.UTC)
        );
    }

    public Tuple toTuple(PowerConsumptionData proto) {
        return Tuple.of(
            proto.getDeviceId(),
            proto.getVoltage(),
            proto.getCurrent(),
            proto.getPower(),
            toOffsetDateTime(proto.getTimestamp())
        );
    }

    public AirQualityEntity toEntity(AirQualityData proto) {
        return new AirQualityEntity(
            proto.getDeviceId(),
            proto.getCo2(),
            proto.getPm25(),
            proto.getPm10(),
            proto.getTvoc(),
            proto.getTemperature(),
            proto.getHumidity(),
            toInstant(proto.getTimestamp())
        );
    }

    public Tuple toTuple(AirQualityEntity entity) {
        return Tuple.wrap(new Object[] {
            entity.deviceId,
            entity.co2,
            entity.pm25,
            entity.pm10,
            entity.tvoc,
            entity.temperature,
            entity.humidity,
            entity.timestamp.atOffset(ZoneOffset.UTC)
        });
    }

    public Tuple toTuple(AirQualityData proto) {
        return Tuple.wrap(new Object[] {
           proto.getDeviceId(),
           proto.getCo2(),
           proto.getPm25(),
           proto.getPm10(),
           proto.getTvoc(),
           proto.getTemperature(),
           proto.getHumidity(),
           toOffsetDateTime(proto.getTimestamp())
        });
    }

    public BatteryDataEntity toEntity(BatteryData proto) {
        return new BatteryDataEntity(
            proto.getDeviceId(),
            proto.getVal(),
            toInstant(proto.getTimestamp())
        );
    }

    public Tuple toTuple(BatteryDataEntity entity) {
        return Tuple.of(
            entity.deviceId,
            entity.val,
            entity.timestamp.atOffset(ZoneOffset.UTC)
        );
    }

    public Tuple toTuple(BatteryData proto) {
        return Tuple.of(
            proto.getDeviceId(),
            proto.getVal(),
            toOffsetDateTime(proto.getTimestamp())
        );
    }

    public SmokeDetectorEntity toEntity(SmokeDetectorData proto) {
        return new SmokeDetectorEntity(
            proto.getDeviceId(),
            proto.getSmokeRaw(),
            proto.getCoLevel(),
            toInstant(proto.getTimestamp())
        );
    }

    public Tuple toTuple(SmokeDetectorData proto) {
        return Tuple.of(
            proto.getDeviceId(),
            proto.getSmokeRaw(),
            proto.getCoLevel(),
            toOffsetDateTime(proto.getTimestamp())
        );
    }

    public Tuple toTuple(SmokeDetectorEntity entity) {
        return Tuple.of(
            entity.deviceId,
            entity.smokeRaw,
            entity.coLevel,
            entity.timestamp.atOffset(ZoneOffset.UTC)
        );
    }

    private Instant toInstant(com.google.protobuf.Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(
            protoTimestamp.getSeconds(),
            protoTimestamp.getNanos()
        );
    }

    private OffsetDateTime toOffsetDateTime(com.google.protobuf.Timestamp timestamp) {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()), ZoneOffset.UTC);
    }
}
