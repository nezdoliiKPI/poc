package dev.nez.panel.dto.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record PowerThresholds(
    @Min(value = 0, message = "deviceId cannot be negative")
    Long deviceId,

    @NotNull(message = "minVoltage cannot be null")
    @Min(value = 0, message = "minVoltage cannot be negative")
    Float minVoltage,

    @NotNull(message = "maxVoltage cannot be null")
    @Min(value = 0, message = "maxVoltage cannot be negative")
    Float maxVoltage,

    @NotNull(message = "maxCurrent cannot be null")
    @Min(value = 0, message = "maxCurrent cannot be negative")
    Float maxCurrent,

    @NotNull(message = "maxPower cannot be null")
    @Min(value = 0, message = "maxPower cannot be negative")
    Float maxPower
) {}