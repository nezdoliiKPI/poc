package dev.nez.producer.dto.rest;

public record LoginResponse(
    Long deviceId,
    String token,
    String topic
) {
}
