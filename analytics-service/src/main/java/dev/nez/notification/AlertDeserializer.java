package dev.nez.notification;

import dev.nez.analytics.data.JsonDeserializer;

public class AlertDeserializer extends JsonDeserializer<Alert> {
    public AlertDeserializer() {
        super(Alert.class);
    }
}
