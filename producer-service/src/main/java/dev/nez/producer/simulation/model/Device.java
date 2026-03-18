package dev.nez.producer.simulation.model;

import io.smallrye.common.constraint.Nullable;

public record Device(
    String hardwareId,
    String password,
    String topic,
    @Nullable
    String batteryTopic
) {
    public Device(
        String hardwareId,
        String password,
        String topic
    ) {
        this(hardwareId, password, topic, null);
    }
}
