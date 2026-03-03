package dev.nez.simulation.dto.rest;

public record LoginRequest(
    String hardwareId,
    String password
) {
}
