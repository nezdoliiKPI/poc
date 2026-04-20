package dev.nez.consumer;
import dev.nez.consumer.entity.AirQualityEntity;
import dev.nez.consumer.entity.BatteryDataEntity;
import dev.nez.consumer.entity.PowerConsumptionEntity;
import dev.nez.consumer.entity.SmokeDetectorEntity;
import dev.nez.proto.timeddata.AirQualityData;
import dev.nez.proto.timeddata.BatteryData;
import dev.nez.proto.timeddata.PowerConsumptionData;

import dev.nez.proto.timeddata.SmokeDetectorData;
import jakarta.inject.Singleton;

import io.vertx.mutiny.sqlclient.Tuple;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

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

    public Tuple toTuple(AirQualityData proto) {
        return Tuple.wrap(List.of(
           proto.getDeviceId(),
           proto.getCo2(),
           proto.getPm25(),
           proto.getPm10(),
           proto.getTvoc(),
           proto.getTemperature(),
           proto.getHumidity(),
           toOffsetDateTime(proto.getTimestamp())
        ));
    }

    public BatteryDataEntity toEntity(BatteryData proto) {
        return new BatteryDataEntity(
            proto.getDeviceId(),
            proto.getVal(),
            toInstant(proto.getTimestamp())
        );
    }

    public Tuple toTuple(BatteryData proto) {
        return Tuple.of(
            proto.getDeviceId(),
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
