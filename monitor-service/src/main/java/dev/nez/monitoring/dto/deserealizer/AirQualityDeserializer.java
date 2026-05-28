package dev.nez.monitoring.dto.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.AirQualityData;
import dev.nez.monitoring.dto.AirQualityPoint;
import org.apache.kafka.common.serialization.Deserializer;

public class AirQualityDeserializer implements Deserializer<AirQualityPoint> {
    @Override
    public AirQualityPoint deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final var proto = AirQualityData.parseFrom(data);
                return new AirQualityPoint(
                    ProtoUtils.toInstant(proto.getTimestamp()),
                    proto.getDeviceId(),
                    proto.getCo2(),
                    proto.getPm25(),
                    proto.getPm10(),
                    proto.getTvoc(),
                    proto.getTemperature(),
                    proto.getHumidity()
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing AirQualityData", e);
        }
    }
}
