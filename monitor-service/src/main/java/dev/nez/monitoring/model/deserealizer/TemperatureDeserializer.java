package dev.nez.monitoring.model.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.TemperatureData;
import dev.nez.monitoring.model.TemperaturePoint;
import org.apache.kafka.common.serialization.Deserializer;

public class TemperatureDeserializer implements Deserializer<TemperaturePoint> {
    @Override
    public TemperaturePoint deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final var proto = TemperatureData.parseFrom(data);
                return new TemperaturePoint(
                    ProtoUtils.toInstant(proto.getTimestamp()),
                    proto.getDeviceId(),
                    proto.getTemperature(),
                    proto.getHumidity()
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing TemperatureData", e);
        }
    }
}