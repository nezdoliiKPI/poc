package dev.nez.analytics.data.deserializer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.AirQualityData;
import org.apache.kafka.common.serialization.Deserializer;

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
