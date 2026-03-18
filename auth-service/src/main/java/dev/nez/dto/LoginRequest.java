package dev.nez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
    String hardwareId,
    String password
) {
}
