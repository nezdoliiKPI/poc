package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Battery(
    Long id,
    Float v
) {
}
