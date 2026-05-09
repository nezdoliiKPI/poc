package dev.nez.consumer.deserealizer;

import dev.nez.dto.proto.timeddata.BatteryData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class BatteryDataDeserializer implements Deserializer<BatteryData> {
    @Override
    public BatteryData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : BatteryData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing BatteryData", e);
        }
    }
}
