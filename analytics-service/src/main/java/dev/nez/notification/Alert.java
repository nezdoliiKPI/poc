package dev.nez.notification;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.UUID;

/**
 * A record representing an alert in the system.
 *
 * @param alertUuid The unique identifier of the alert event.
 * @param dID       The unique identifier of the device associated with the alert.
 * @param metric    The name of the metric that triggered the alert (e.g., "co2", "temperature").
 * @param val       The actual numerical value that breached the threshold.
 * @param sev       The severity level of the alert (e.g., "WARNING", "CRITICAL", "FAULT").
 * @param msg       A descriptive message regarding the incident or event.
 * @param ts        The timestamp when the alert was generated.
 */
@RegisterForReflection
public record Alert(
    UUID alertUuid,
    Long dID,
    String metric,
    Float val,
    Severity sev,
    String msg,
    Instant ts
) {
    public enum Severity {
        WARNING, CRITICAL, FAULT
    }
}
