package dev.nez.simulation.model;

import java.util.concurrent.TimeUnit;

public record MessageTiming(
    TimeUnit unit,
    long initialDelay,
    long period,
    long messageTtlSeconds
) {
}
