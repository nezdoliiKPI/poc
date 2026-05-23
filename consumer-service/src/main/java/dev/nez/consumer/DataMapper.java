package dev.nez.consumer;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.*;

import jakarta.inject.Singleton;

import io.vertx.mutiny.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Singleton
public class DataMapper {
    public Tuple toTuple(PowerConsumptionData proto) {
        return Tuple.of(
            proto.getDeviceId(),
            proto.getVoltage(),
            proto.getCurrent(),
            proto.getPower(),
            toOffsetDateTime(proto.getTimestamp())
        );
    }

    public Tuple toTuple(TemperatureData proto) {
        return Tuple.wrap(new Object[] {
            proto.getDeviceId(),
            proto.getTemperature(),
            proto.getHumidity(),
            toOffsetDateTime(proto.getTimestamp())
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

    public Tuple toTuple(BatteryData proto) {
        return Tuple.of(
            proto.getDeviceId(),
            proto.getVal(),
            toOffsetDateTime(proto.getTimestamp())
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

    private OffsetDateTime toOffsetDateTime(com.google.protobuf.Timestamp timestamp) {
        return OffsetDateTime.ofInstant(
            ProtoUtils.toInstant(timestamp), ZoneOffset.UTC);
    }
}
