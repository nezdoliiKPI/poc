package dev.nez.analytics.data.alert;

import dev.nez.analytics.data.JsonDeserializer;

public class AlertDeserializer extends JsonDeserializer<Alert> {
    public AlertDeserializer() {
        super(Alert.class);
    }
}
