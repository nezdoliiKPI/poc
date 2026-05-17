package dev.nez.analytics.data.power;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PowerThresholds(
    Float minVoltage,
    Float maxVoltage,
    Float maxCurrent,
    Float maxPower
) {
}
