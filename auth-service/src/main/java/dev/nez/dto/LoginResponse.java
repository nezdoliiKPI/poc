package dev.nez.dto;

public record LoginResponse(
        Long deviceId,
        String token
) {}
