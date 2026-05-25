package dev.nez.configuration.dto.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public record AirQualityThresholds(
    @Min(value = 0, message = "deviceId cannot be negative")
    Long deviceId,

    @NotNull(message = "maxCo2 cannot be null")
    @Min(value = 0, message = "maxCo2 cannot be negative")
    Integer maxCo2,

    @NotNull(message = "maxPm25 cannot be null")
    @Min(value = 0, message = "maxPm25 cannot be negative")
    Float maxPm25,

    @NotNull(message = "maxPm10 cannot be null")
    @Min(value = 0, message = "maxPm10 cannot be negative")
    Float maxPm10,

    @NotNull(message = "maxTvoc cannot be null")
    @Min(value = 0, message = "maxTvoc cannot be negative")
    Float maxTvoc,

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
