package dev.nez.monitoring.dto;

import java.time.Instant;

public record TemperaturePoint(
    Instant timeDate,
    long    deviceId,
    float   temperature,
    float   humidity
) {}