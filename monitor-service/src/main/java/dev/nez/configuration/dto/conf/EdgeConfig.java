package dev.nez.configuration.dto.conf;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EdgeConfig(
    @NotBlank(message = "Topic name cannot be null or blank")
    String topic,

    @NotBlank(message = "Consume flag is required")
    Boolean consume,

    @NotBlank(message = "Threshold is required")
    @PositiveOrZero(message = "Threshold cannot be negative")
    Integer threshold
) {
}
