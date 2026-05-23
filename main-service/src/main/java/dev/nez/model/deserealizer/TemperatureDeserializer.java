package dev.nez.model.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.TemperatureData;
import org.apache.kafka.common.serialization.Deserializer;

public class TemperatureDeserializer implements Deserializer<TemperatureData> {
    @Override
    public TemperatureData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : TemperatureData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing TemperatureData", e);
        }
    }
}
