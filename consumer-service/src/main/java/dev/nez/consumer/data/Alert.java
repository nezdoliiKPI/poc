package dev.nez.consumer.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.UUID;

/**
 * A record representing an alert in the system.
 *
 * @param uuid      The unique identifier of the alert event.
 * @param dID       The unique identifier of the device associated with the alert.
 * @param metric    The name of the metric that triggered the alert (e.g., "co2", "temperature").
 * @param val       The actual numerical value that breached the threshold.
 * @param min       The minimum allowable threshold for the metric.
 * @param max       The maximum allowable threshold for the metric.
 * @param sev       The severity level of the alert (e.g., "WARNING", "CRITICAL", "FAULT").
 * @param msg       A descriptive message regarding the incident or event.
 * @param ts        The timestamp when the data was received.
 */
@RegisterForReflection
public record Alert(
    UUID uuid,
    Long dID,
    String metric,
    Float val,
    Float min,
    Float max,
    Severity sev,
    String msg,
    Instant ts
) {
    public enum Severity {
        WARNING, CRITICAL, FAULT
    }
}
