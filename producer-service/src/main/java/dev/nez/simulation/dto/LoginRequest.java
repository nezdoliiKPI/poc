package dev.nez.simulation.dto;

public record LoginRequest(
    String hardwareId,
    String password
) {
}
