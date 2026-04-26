package dev.nez.producer.simulation.generator.data;

import dev.nez.producer.dto.mqtt.Battery;
import dev.nez.producer.dto.mqtt.SmokeDetector;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.concurrent.TimeUnit;

public class SmokeDataGenerator extends DeviceDataGenerator {

    public SmokeDataGenerator(
            String hardwareId,
            String password,
            String topic,
            String batteryTopic,
            MessageType messageType
    ) {
        final var device = new Device(hardwareId, password, topic, batteryTopic);
        final var mainTiming = new MessageTiming(TimeUnit.MINUTES, 0, 30, TimeUnit.MINUTES.toSeconds(30));
        final var batteryTiming = new MessageTiming(TimeUnit.HOURS, 0, 12, TimeUnit.HOURS.toSeconds(12));

        super(device, messageType, mainTiming, batteryTiming);
    }

    @Override
    public Object getData() {
        final var sr = 15 + rnd.nextInt(26);
        final var co = rnd.nextInt(10) > 8 ? 1 + rnd.nextInt(2) : 0;

        return new SmokeDetector(this.deviceId, sr, co);
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 99.0f);
    }
}
