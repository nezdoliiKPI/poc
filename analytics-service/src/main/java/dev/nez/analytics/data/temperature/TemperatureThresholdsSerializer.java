package dev.nez.analytics.data.temperature;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class TemperatureThresholdsSerializer implements Serializer<TemperatureThresholds> {
    @Override
    public byte[] serialize(String topic, TemperatureThresholds data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
