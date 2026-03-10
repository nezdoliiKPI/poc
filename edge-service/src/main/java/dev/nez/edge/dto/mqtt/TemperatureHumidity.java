package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TemperatureHumidity(
    Long id,
    Float t,
    Float h
) {
}
