package dev.nez.edge.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Temperature(
    Long deviceId,
    Double temp
) {
}
