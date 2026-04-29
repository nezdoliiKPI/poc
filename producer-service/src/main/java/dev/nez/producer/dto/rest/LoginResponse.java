package dev.nez.producer.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
    Long deviceId,
    String token,
    String topic
) {
}
