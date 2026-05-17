package dev.nez.analytics.data.power;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class PowerThresholdsSerializer implements Serializer<PowerThresholds> {
    @Override
    public byte[] serialize(String topic, PowerThresholds data) {
        return data == null ? null : Json.encodeToBuffer(data).getBytes();
    }
}
