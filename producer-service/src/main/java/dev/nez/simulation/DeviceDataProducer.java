package dev.nez.simulation;

import dev.nez.simulation.model.Device;
import io.smallrye.common.constraint.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.Random;

public abstract class DeviceDataProducer {
    protected final Random random = new Random();

    public final Device device;
    public final MESSAGE_TYPE messageType;

    public final TimeUnit unit;
    public final long initialDelay;
    public final long period;
    public final long messageTtlSeconds;

    public Long deviceId = null;
    public String token = null;

    protected DeviceDataProducer(
        @NotNull Device device,
        @NotNull MESSAGE_TYPE messageType,
        @NotNull TimeUnit unit,
        long initialDelay,
        long period,
        long messageTtlSeconds
    ) {
        this.device = device;
        this.messageType = messageType;
        this.unit = unit;
        this.initialDelay = initialDelay;
        this.period = period;
        this.messageTtlSeconds = messageTtlSeconds;
    }

    public abstract Object getData();

    public enum MESSAGE_TYPE {
        JSON, PROTO
    }
}
