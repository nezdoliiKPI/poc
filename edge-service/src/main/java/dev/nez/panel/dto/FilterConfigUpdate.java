package dev.nez.panel.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@RegisterForReflection
public record FilterConfigUpdate(
    @NotBlank(message = "Topic name cannot be null or blank")
    String topic,

    @NotNull(message = "Consume flag is required")
    Boolean consume,

    @NotNull(message = "Threshold is required")
    @PositiveOrZero(message = "Threshold cannot be negative")
    Integer threshold
) {
}
