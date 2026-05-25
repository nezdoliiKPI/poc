package dev.nez.edge.dto;

import dev.nez.edge.dto.rest.FilterConfig;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class FilterConfigDeserializer implements Deserializer<FilterConfig> {

    @Override
    public FilterConfig deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? Json.decodeValue(Buffer.buffer(data), FilterConfig.class) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to FilterConfig", e);
        }
    }
}
