package dev.nez.panel.dto.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record BatteryThresholds(
    @Min(value = 0, message = "deviceId cannot be negative")
    Long deviceId,

    @NotNull(message = "minBatteryLevel cannot be null")
    @Min(value = 0, message = "minBatteryLevel cannot be negative")
    Float minBatteryLevel
) {}