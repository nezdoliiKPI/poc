package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Air Quality message body
 *
 * @param id    unique device ID
 * @param co2   CO2 concentration in ppm
 * @param pm25  PM2.5 particles in µg/m³
 * @param pm10  PM10 particles in µg/m³
 * @param tvoc  Total Volatile Organic Compounds in mg/m³
 * @param t     ambient t in Celsius
 * @param h     relative h percentage (0.0 to 100.0)
 */
@RegisterForReflection
public record AirQuality(
    Long id,
    Integer co2,
    Float pm25,
    Float pm10,
    Float tvoc,
    Float t,
    Float h
) {
}