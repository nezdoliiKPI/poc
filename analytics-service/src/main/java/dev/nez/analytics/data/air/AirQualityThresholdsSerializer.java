package dev.nez.analytics.data.air;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class AirQualityThresholdsSerializer implements Serializer<AirQualityThresholds> {
    @Override
    public byte[] serialize(String topic, AirQualityThresholds data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
