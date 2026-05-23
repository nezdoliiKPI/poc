package dev.nez.model;

import java.time.Instant;

public record PowerConsumptionPoint(
    Instant timeDate,
    long    deviceId,
    float   voltage,
    float   current,
    float   power
) {}