package dev.nez.monitoring.model;

import java.time.Instant;

public record TemperaturePoint(
    Instant timeDate,
    long    deviceId,
    float   temperature,
    float   humidity
) {}