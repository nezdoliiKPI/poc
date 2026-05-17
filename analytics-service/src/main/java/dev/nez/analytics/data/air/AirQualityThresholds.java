package dev.nez.analytics.data.air;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AirQualityThresholds(
    int maxCo2,
    float maxPm25,
    float maxPm10,
    float maxTvoc,
    float minTemperature,
    float maxTemperature,
    float minHumidity,
    float maxHumidity
) {}
