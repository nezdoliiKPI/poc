package dev.nez.monitoring.dto.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.BatteryData;
import dev.nez.monitoring.dto.BatteryPoint;
import org.apache.kafka.common.serialization.Deserializer;

public class BatteryDataDeserializer implements Deserializer<BatteryPoint> {
    @Override
    public BatteryPoint deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final var proto = BatteryData.parseFrom(data);
                return new BatteryPoint(
                    ProtoUtils.toInstant(proto.getTimestamp()),
                    proto.getDeviceId(),
                    proto.getVal()
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing BatteryData", e);
        }
    }
}