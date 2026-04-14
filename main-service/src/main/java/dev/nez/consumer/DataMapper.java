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

import java.time.Instant;

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

    public BatteryDataEntity toEntity(BatteryData proto) {
        return new BatteryDataEntity(
            proto.getDeviceId(),
            proto.getVal(),
            toInstant(proto.getTimestamp())
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

    private Instant toInstant(com.google.protobuf.Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(
            protoTimestamp.getSeconds(),
            protoTimestamp.getNanos()
        );
    }
}
