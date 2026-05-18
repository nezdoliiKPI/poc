package dev.nez.analytics.data.temperature;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TemperatureThresholds(
    Float minTemperature,
    Float maxTemperature,
    Float minHumidity,
    Float maxHumidity
) {}
