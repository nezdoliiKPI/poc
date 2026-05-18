package dev.nez.notification;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record Alert(
    Long deviceId,
    List<String> messages
) {
}
