package dev.nez.simulation.dto;

public record LoginResponse(
    Long deviceId,
    String token,
    String topic
) {
}
