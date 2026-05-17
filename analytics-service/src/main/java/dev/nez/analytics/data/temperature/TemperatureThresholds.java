package dev.nez.analytics.data.temperature;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TemperatureThresholds(
    float minTemperature,
    float maxTemperature,
    float minHumidity,
    float maxHumidity
) {}
