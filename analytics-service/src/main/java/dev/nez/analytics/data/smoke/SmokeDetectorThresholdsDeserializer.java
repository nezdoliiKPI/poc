package dev.nez.analytics.data.smoke;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class SmokeDetectorThresholdsDeserializer implements Deserializer<SmokeDetectorThresholds> {
    @Override
    public SmokeDetectorThresholds deserialize(String topic, byte[] data) {
        return data != null ? Json.decodeValue(Buffer.buffer(data), SmokeDetectorThresholds.class) : null;
    }
}
