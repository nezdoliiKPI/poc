package dev.nez.analytics.data.power;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import org.apache.kafka.common.serialization.Deserializer;

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
