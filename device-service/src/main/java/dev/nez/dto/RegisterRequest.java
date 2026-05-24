package dev.nez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.nez.model.Device.MessageType;
import io.smallrye.common.constraint.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
    @NotBlank(message = "Hardware ID is required and cannot be empty")
    String hardwareId,

    @NotBlank(message = "Password is required and cannot be empty")
    String password,

    @NotBlank(message = "Topic is required and cannot be empty")
    String topic,

    @Nullable
    String batteryTopic,

    @NotNull(message = "Message type is required")
    MessageType messageType
) {
}