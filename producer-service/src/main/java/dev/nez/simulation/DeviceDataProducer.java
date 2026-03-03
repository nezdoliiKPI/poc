package dev.nez.simulation;

import dev.nez.simulation.model.Device;
import dev.nez.simulation.model.MessageTiming;
import io.smallrye.common.constraint.NotNull;

import java.util.Random;

public abstract class DeviceDataProducer {
    protected final Random random = new Random();

    public final Device device;
    public final MESSAGE_TYPE messageType;
    public final MessageTiming mainTiming;

    public Long deviceId = null;
    public String token = null;

    protected DeviceDataProducer(
        @NotNull Device device,
        @NotNull MESSAGE_TYPE messageType,
        @NotNull MessageTiming mainTiming
    ) {
        this.device = device;
        this.messageType = messageType;
        this.mainTiming = mainTiming;
    }

    public abstract Object getData();

    public enum MESSAGE_TYPE {
        JSON, PROTO
    }
}
