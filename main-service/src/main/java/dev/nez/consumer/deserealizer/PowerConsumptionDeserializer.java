package dev.nez.consumer.deserealizer;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PowerConsumptionDeserializer implements Deserializer<PowerConsumptionData> {
    @Override
    public PowerConsumptionData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : PowerConsumptionData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing PowerConsumptionData", e);
        }
    }
}
