package dev.nez.alert;

import io.vertx.core.json.Json;
import org.apache.kafka.common.serialization.Serializer;

public class AlertSerializer implements Serializer<Alert> {
    @Override
    public byte[] serialize(String topic, Alert data) {
        return data != null ? Json.encodeToBuffer(data).getBytes() : null;
    }
}
