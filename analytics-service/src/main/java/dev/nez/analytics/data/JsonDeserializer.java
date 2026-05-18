package dev.nez.analytics.data;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonDeserializer<T> implements Deserializer<T> {
    private final Class<T> targetClass;

    public JsonDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return data != null ? Json.decodeValue(Buffer.buffer(data), targetClass) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to TemperatureThresholds", e);
        }
    }
}
