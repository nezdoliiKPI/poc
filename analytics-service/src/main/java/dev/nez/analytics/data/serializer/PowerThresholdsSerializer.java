package dev.nez.analytics.data.serializer;

import dev.nez.analytics.data.event.PowerThresholds;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class PowerThresholdsSerializer implements Serializer<PowerThresholds> {
    @Override
    public byte[] serialize(String topic, PowerThresholds data) {
        return data == null ? null : Json.encodeToBuffer(data).getBytes();
    }
}
