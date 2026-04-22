package dev.nez.panel.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ConfigUpdate(
    Integer airJsonCount,
    Integer airProtoCount,
    Integer powerJsonCount,
    Integer powerProtoCount,
    Integer smokeJsonCount,
    Integer smokeProtoCount
) {
}
