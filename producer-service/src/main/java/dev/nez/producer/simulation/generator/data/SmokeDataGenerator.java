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
        final var mainTiming = new MessageTiming(TimeUnit.MINUTES, 0, 10, TimeUnit.MINUTES.toSeconds(10));
        final var batteryTiming = new MessageTiming(TimeUnit.HOURS, 0, 1, TimeUnit.HOURS.toSeconds(1));

        super(device, messageType, mainTiming, batteryTiming);
    }

    @Override
    public Object getData() {
        final var sr = 15;
        final var co = 3;

        return new SmokeDetector(
            this.deviceId,
            sr + rnd.nextInt(20),
            co + rnd.nextInt(-1, 2)
        );
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 75.0f);
    }
}
