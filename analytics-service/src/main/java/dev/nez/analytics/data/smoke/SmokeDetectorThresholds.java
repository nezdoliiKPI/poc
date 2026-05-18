package dev.nez.analytics.data.smoke;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SmokeDetectorThresholds(
    Integer maxSmokeRaw,
    Integer maxCoLevel
) {}
