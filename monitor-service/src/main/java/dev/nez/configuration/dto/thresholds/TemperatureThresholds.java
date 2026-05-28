package dev.nez.configuration.dto.thresholds;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record TemperatureThresholds(
    @Min(value = 0, message = "deviceId cannot be negative")
    Long deviceId,

    @NotNull(message = "minTemperature cannot be null")
    @Min(value = -100, message = "minTemperature is too low")
    @Max(value = 150, message = "minTemperature is too high")
    Float minTemperature,

    @NotNull(message = "maxTemperature cannot be null")
    @Min(value = -100, message = "maxTemperature is too low")
    @Max(value = 150, message = "maxTemperature is too high")
    Float maxTemperature,

    @NotNull(message = "minHumidity cannot be null")
    @Min(value = 0, message = "minHumidity cannot be less than 0")
    @Max(value = 100, message = "minHumidity cannot be more than 100")
    Float minHumidity,

    @NotNull(message = "maxHumidity cannot be null")
    @Min(value = 0, message = "maxHumidity cannot be less than 0")
    @Max(value = 100, message = "maxHumidity cannot be more than 100")
    Float maxHumidity
) {}