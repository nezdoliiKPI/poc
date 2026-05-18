package dev.nez.panel.dto;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerializer<T> implements Serializer<T> {

    @Override
    public byte[] serialize(String topic, T data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
