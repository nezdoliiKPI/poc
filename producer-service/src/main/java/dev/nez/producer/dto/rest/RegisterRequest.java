package dev.nez.producer.dto.rest;

import dev.nez.producer.simulation.generator.data.DeviceDataGenerator.MessageType;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic,
    String batteryTopic,
    MessageType messageType
) {
}
