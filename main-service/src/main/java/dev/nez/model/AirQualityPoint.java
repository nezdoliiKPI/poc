package dev.nez.model;

import java.time.Instant;

public record AirQualityPoint(
    Instant timeDate,
    long    deviceId,
    int     co2,
    float   pm25,
    float   pm10,
    float   tvoc,
    float   temperature,
    float   humidity
) {}