package dev.nez.simulation.dto.rest;

public record LoginResponse(
    Long deviceId,
    String token,
    String topic
) {
}
