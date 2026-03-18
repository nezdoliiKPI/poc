package dev.nez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.nez.model.Device.MessageType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
    String hardwareId,
    String password,
    String topic,
    String batteryTopic,
    MessageType messageType
) {
}