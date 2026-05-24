package dev.nez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
    @NotBlank(message = "Hardware ID is required and cannot be empty")
    String hardwareId,

    @NotBlank(message = "Password is required and cannot be empty")
    String password
) {
}
