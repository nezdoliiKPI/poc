package dev.nez.producer.dto.rest;

public record LoginRequest(
    String hardwareId,
    String password
) {
}
