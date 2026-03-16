package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Power Consumption message body
 *
 * @param id    unique device ID
 * @param cv    current voltage (V)
 * @param cf    current flow (A)
 * @param pow   active power (W)
 *
 */
@RegisterForReflection
public record PowerConsumption(
    Long id,
    Float cv,
    Float cf,
    Float pow
) {
}
