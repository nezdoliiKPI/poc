package dev.nez.consumer.deserealizer;

import dev.nez.dto.proto.timeddata.AirQualityData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;

public class AirQualityDeserializer implements Deserializer<AirQualityData> {
    @Override
    public AirQualityData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : AirQualityData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing AirQualityData", e);
        }
    }
}
