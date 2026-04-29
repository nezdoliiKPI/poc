package dev.nez.panel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@JsonIgnoreProperties(ignoreUnknown = true)
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
