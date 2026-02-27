package dev.nez.simulation.dto;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic
) {
}
