package dev.nez.analytics.data.temperature;

import dev.nez.dto.proto.timeddata.TemperatureData;
import org.apache.kafka.common.serialization.Serializer;

public class TemperatureSerializer implements Serializer<TemperatureData> {
    @Override
    public byte[] serialize(String topic, TemperatureData data) {
        return data != null ? data.toByteArray() : null;
    }
}
