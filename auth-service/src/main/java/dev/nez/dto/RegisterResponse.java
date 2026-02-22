package dev.nez.dto;

public record RegisterResponse(
    Long deviceId,
    String hardwareId,
    String password,
    String topic
) {
}
