package dev.nez.analytics.data.battery;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record BatteryThresholds(
    Float minBatteryLevel
) {}
