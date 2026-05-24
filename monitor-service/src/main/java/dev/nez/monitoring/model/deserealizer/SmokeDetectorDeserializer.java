package dev.nez.monitoring.model.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import dev.nez.monitoring.model.SmokeDetectorPoint;
import org.apache.kafka.common.serialization.Deserializer;

public class SmokeDetectorDeserializer implements Deserializer<SmokeDetectorPoint> {
    @Override
    public SmokeDetectorPoint deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final var proto = SmokeDetectorData.parseFrom(data);
                return new SmokeDetectorPoint(
                    ProtoUtils.toInstant(proto.getTimestamp()),
                    proto.getDeviceId(),
                    proto.getSmokeRaw(),
                    proto.getCoLevel()
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing SmokeDetectorData", e);
        }
    }
}