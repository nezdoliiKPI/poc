package dev.nez.producer.simulation.generator;

import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.common.constraint.Nullable;

import java.util.Random;

public abstract class DeviceDataGenerator {
    protected final Random random = new Random();

    public final Device device;
    public final MessageType messageType;
    public final MessageTiming mainTiming;
    public final MessageTiming batteryTiming;

    public Long deviceId = null;
    public String token = null;

    public enum MessageType {
        JSON, PROTO
    }

    protected DeviceDataGenerator(
        @NotNull Device device,
        @NotNull MessageType messageType,
        @NotNull MessageTiming mainTiming,
        @Nullable MessageTiming batteryTiming
    ) {
        this.device = device;
        this.messageType = messageType;
        this.mainTiming = mainTiming;
        this.batteryTiming = batteryTiming;

        if (this.device.batteryTopic() != null && batteryTiming == null) {
            throw  new IllegalArgumentException("batteryTopic is required, but batteryTiming is null");
        }
        if (this.device.batteryTopic() == null && batteryTiming != null) {
            throw  new IllegalArgumentException("batteryTiming is required, but batteryTopic is null");
        }
    }

    protected DeviceDataGenerator(
        @NotNull Device device,
        @NotNull MessageType messageType,
        @NotNull MessageTiming mainTiming
    ) {
        this(device, messageType, mainTiming, null);
    }

    public boolean batteryIsPresent() {
        return batteryTiming != null;
    }

    public abstract Object getData();

    public Object getBatteryData() {
        if (batteryTiming != null) {
            throw new UnsupportedOperationException(
                    "BatteryTiming is already set, but getBatteryData() not implemented yet.");
        } else  {
            throw  new UnsupportedOperationException(
                    "BatteryTiming is null");
        }
    }
}
