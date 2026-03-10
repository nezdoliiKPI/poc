package dev.nez.simulation.dto.rest;

import dev.nez.simulation.DeviceDataProducer.MessageType;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic,
    String batteryTopic,
    MessageType messageType
) {
}
