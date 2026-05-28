package dev.nez.consumer.deserializer;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;

public class PowerConsumptionDeserializer implements Deserializer<PowerConsumptionData> {
    @Override
    public PowerConsumptionData deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? PowerConsumptionData.parseFrom(data) : null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing PowerConsumptionData", e);
        }
    }
}
