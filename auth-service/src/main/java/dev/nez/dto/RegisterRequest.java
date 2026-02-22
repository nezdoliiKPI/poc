package dev.nez.dto;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic
) {
}