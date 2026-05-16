package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Represents a single reading of temperature (and optional humidity) telemetry from a device.
 *
 * @param id          The unique identifier of the sensor or device.
 * @param t The ambient temperature in degrees Celsius (°C).
 * @param h    The relative humidity percentage (%).
 */
@RegisterForReflection
public record Temperature(
    Long id,
    Float t,
    Float h
) {
}
