package dev.nez.analytics.data.power;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class PowerThresholdsDeserializer implements Deserializer<PowerThresholds> {
    @Override
    public PowerThresholds deserialize(String topic, byte[] data) {
        return data == null ? null : Json.decodeValue(Buffer.buffer(data), PowerThresholds.class);
    }
}
