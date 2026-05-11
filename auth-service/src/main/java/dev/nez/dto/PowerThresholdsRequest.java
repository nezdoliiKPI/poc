package dev.nez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record PowerThresholdsRequest(
    @NotNull(message = "device_id is required")
    Long deviceId,

    @Positive(message = "Minimum voltage must be positive")
    Float minVoltage,

    @Positive(message = "Maximum voltage must be positive")
    Float maxVoltage,

    @NotNull(message = "Maximum current is required")
    @PositiveOrZero(message = "Current cannot be negative")
    Float maxCurrent,

    @NotNull(message = "Maximum power is required")
    @PositiveOrZero(message = "Power cannot be negative")
    Float maxPower
) {
}
