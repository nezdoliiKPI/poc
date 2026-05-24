package dev.nez.monitoring.model;

import java.time.Instant;

public record BatteryPoint(
    Instant timeDate,
    long    deviceId,
    float   val
) {}