package dev.nez.analytics.data.battery;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class BatteryThresholdsDeserializer implements Deserializer<BatteryThresholds> {
    @Override
    public BatteryThresholds deserialize(String topic, byte[] data) {
        return data != null ? Json.decodeValue(Buffer.buffer(data), BatteryThresholds.class) : null;
    }
}
