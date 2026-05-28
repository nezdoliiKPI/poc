package dev.nez.monitoring.dto;

import java.time.Instant;

public record BatteryPoint(
    Instant timeDate,
    long    deviceId,
    float   val
) {}