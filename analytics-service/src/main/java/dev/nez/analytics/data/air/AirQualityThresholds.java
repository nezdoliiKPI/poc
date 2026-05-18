package dev.nez.analytics.data.air;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AirQualityThresholds(
    Integer maxCo2,
    Float maxPm25,
    Float maxPm10,
    Float maxTvoc,
    Float minTemperature,
    Float maxTemperature,
    Float minHumidity,
    Float maxHumidity
) {}
