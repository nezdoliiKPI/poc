package dev.nez.model;

import java.time.Instant;

public record SmokeDetectorPoint(
    Instant timeDate,
    long    deviceId,
    int     smokeRaw,
    int     coLevel
) {}