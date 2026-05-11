package dev.nez.analytics.data.event;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PowerThresholds(
    Long deviceId,
    Float minVoltage,
    Float maxVoltage,
    Float maxCurrent,
    Float maxPower
) {
}
