package dev.nez.dto;

public record LoginRequest(
    String hardwareId,
    String password
) {
}
