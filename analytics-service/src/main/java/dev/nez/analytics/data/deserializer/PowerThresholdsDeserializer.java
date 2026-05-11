package dev.nez.analytics.data.deserializer;

import dev.nez.analytics.data.event.PowerThresholds;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class PowerThresholdsDeserializer implements Deserializer<PowerThresholds> {
    @Override
    public PowerThresholds deserialize(String topic, byte[] data) {
        return data == null ? null : Json.decodeValue(Buffer.buffer(data), PowerThresholds.class);
    }
}
