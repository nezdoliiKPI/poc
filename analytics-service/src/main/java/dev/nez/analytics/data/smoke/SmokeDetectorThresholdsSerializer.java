package dev.nez.analytics.data.smoke;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class SmokeDetectorThresholdsSerializer implements Serializer<SmokeDetectorThresholds> {
    @Override
    public byte[] serialize(String topic, SmokeDetectorThresholds data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
