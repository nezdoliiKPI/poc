package dev.nez.analytics.data.temperature;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class TemperatureThresholdsDeserializer implements Deserializer<TemperatureThresholds> {
    @Override
    public TemperatureThresholds deserialize(String topic, byte[] data) {
        try {
            return data != null ? Json.decodeValue(Buffer.buffer(data), TemperatureThresholds.class) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to TemperatureThresholds", e);
        }
    }
}
