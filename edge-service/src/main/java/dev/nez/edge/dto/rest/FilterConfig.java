package dev.nez.edge.dto.rest;
import io.quarkus.runtime.annotations.RegisterForReflection;


@RegisterForReflection
public record FilterConfig(
    String topic,
    Boolean consume,
    Integer threshold
) {
}
