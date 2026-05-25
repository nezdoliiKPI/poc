package dev.nez.configuration.dto.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record SmokeDetectorThresholds(
    @Min(value = 0, message = "deviceId cannot be negative")
    Long deviceId,

    @NotNull(message = "maxSmokeRaw cannot be null")
    @Min(value = 0, message = "maxSmokeRaw cannot be negative")
    Integer maxSmokeRaw,

    @NotNull(message = "maxCoLevel cannot be null")
    @Min(value = 0, message = "maxCoLevel cannot be negative")
    Integer maxCoLevel
) {}