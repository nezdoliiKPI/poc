package dev.nez.alert;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Deserializer;

public class AlertDeserializer implements Deserializer<Alert> {
    @Override
    public Alert deserialize(String topic, byte[] data) {
        return Json.decodeValue(Buffer.buffer(data), Alert.class);
    }
}
