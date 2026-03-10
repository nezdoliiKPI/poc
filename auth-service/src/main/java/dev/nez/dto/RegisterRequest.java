package dev.nez.dto;

import dev.nez.model.Device.MessageType;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic,
    String batteryTopic,
    MessageType messageType
) {
}