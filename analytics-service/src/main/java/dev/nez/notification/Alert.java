package dev.nez.notification;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.List;

/**
 * A record representing an alert in the system.
 *
 * @param id   The unique identifier of the device (deviceId).
 * @param msg  A list of messages regarding incidents or events (messages).
 * @param ts   The timestamp when the telemetry was generated (telemetryTimestamp).
 */
@RegisterForReflection
public record Alert(
    Long id,
    List<String> msg,
    Instant ts
) {
}
