package dev.nez.consumer.deserializer;

import dev.nez.consumer.data.Alert;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class AlertDeserializer implements Deserializer<Alert> {
    @Override
    public Alert deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? Json.decodeValue(Buffer.buffer(data), Alert.class) : null;
        } catch (DecodeException e) {
            throw new RuntimeException("Error deserializing Alert", e);
        }
    }
}
