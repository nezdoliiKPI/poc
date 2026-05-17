package dev.nez.analytics.data.air;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class AirQualityThresholdsDeserializer implements Deserializer<AirQualityThresholds> {
    @Override
    public AirQualityThresholds deserialize(String topic, byte[] data) {
        return data != null ? Json.decodeValue(Buffer.buffer(data), AirQualityThresholds.class) : null;
    }
}
