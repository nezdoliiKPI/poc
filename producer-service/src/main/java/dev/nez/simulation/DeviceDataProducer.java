package dev.nez.simulation;

import dev.nez.simulation.model.Device;
import io.smallrye.common.constraint.NotNull;
import java.util.concurrent.TimeUnit;

import java.util.Random;

public abstract class DeviceDataProducer {
    protected final Random random = new Random();

    public final Device device;
    public final Long initialDelay;
    public final Long period;
    public final  TimeUnit unit;

    public Long deviceId = null;
    public String token = null;

    protected DeviceDataProducer(
        @NotNull Device device,
        @NotNull Long initialDelay,
        @NotNull Long period,
        @NotNull TimeUnit unit
    ) {
        this.device = device;
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
    }

    public abstract Object getData();
}
