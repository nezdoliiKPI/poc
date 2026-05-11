package dev.nez;

import dev.nez.dto.PowerThresholdsRequest;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class PowerThresholdsSerializer implements Serializer<PowerThresholdsRequest> {
    @Override
    public byte[] serialize(String topic, PowerThresholdsRequest data) {
        return data == null ? null : Json.encodeToBuffer(data).getBytes();
    }
}
