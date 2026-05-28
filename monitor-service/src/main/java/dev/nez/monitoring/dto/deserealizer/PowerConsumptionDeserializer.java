package dev.nez.monitoring.dto.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import dev.nez.monitoring.dto.PowerConsumptionPoint;
import org.apache.kafka.common.serialization.Deserializer;

public class PowerConsumptionDeserializer implements Deserializer<PowerConsumptionPoint> {
    @Override
    public PowerConsumptionPoint deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final var proto = PowerConsumptionData.parseFrom(data);
                return new PowerConsumptionPoint(
                    ProtoUtils.toInstant(proto.getTimestamp()),
                    proto.getDeviceId(),
                    proto.getVoltage(),
                    proto.getCurrent(),
                    proto.getPower()
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing PowerConsumptionData", e);
        }
    }
}