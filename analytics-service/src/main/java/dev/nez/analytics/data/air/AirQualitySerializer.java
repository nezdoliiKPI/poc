package dev.nez.analytics.data.air;

import dev.nez.dto.proto.timeddata.AirQualityData;
import org.apache.kafka.common.serialization.Serializer;

public class AirQualitySerializer implements Serializer<AirQualityData> {
    @Override
    public byte[] serialize(String topic, AirQualityData data) {
        return data == null ? null : data.toByteArray();
    }
}