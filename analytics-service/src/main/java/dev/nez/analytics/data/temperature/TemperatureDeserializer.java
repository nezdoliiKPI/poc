package dev.nez.analytics.data.temperature;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.TemperatureData;
import org.apache.kafka.common.serialization.Deserializer;

public class TemperatureDeserializer implements Deserializer<TemperatureData> {
    @Override
    public TemperatureData deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? TemperatureData.parseFrom(data) : null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to deserialize Protobuf message TemperatureData", e);
        }
    }
}
