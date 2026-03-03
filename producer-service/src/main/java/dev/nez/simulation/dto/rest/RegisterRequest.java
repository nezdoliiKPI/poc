package dev.nez.simulation.dto.rest;

public record RegisterRequest(
    String hardwareId,
    String password,
    String topic
) {
}
