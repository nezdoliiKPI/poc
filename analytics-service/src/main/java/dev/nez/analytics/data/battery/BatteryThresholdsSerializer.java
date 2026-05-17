package dev.nez.analytics.data.battery;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class BatteryThresholdsSerializer implements Serializer<BatteryThresholds> {
    @Override
    public byte[] serialize(String topic, BatteryThresholds data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
