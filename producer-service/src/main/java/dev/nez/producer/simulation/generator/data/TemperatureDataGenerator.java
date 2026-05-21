package dev.nez.producer.simulation.generator.data;

import dev.nez.producer.dto.mqtt.Battery;
import dev.nez.producer.dto.mqtt.Temperature; // Вкажіть правильний пакет для вашого DTO класу Temperature
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TemperatureDataGenerator extends DeviceDataGenerator {

    public TemperatureDataGenerator(
        String hardwareId,
        String password,
        String topic,
        String batteryTopic,
        MessageType messageType
    ) {
        final Random rnd = ThreadLocalRandom.current();

        final var device = new Device(hardwareId, password, topic, batteryTopic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(2), 1, 1);
        final var batteryTiming = new MessageTiming(TimeUnit.MINUTES, 0, 30, 30);

        super(device, messageType, mainTiming, batteryTiming);
    }

    @Override
    public Object getData() {
        final var t = 22.0f;
        final var h = 45.0f;

        final float TEMP_DELTA = 0.5f;
        final float HUMIDITY_DELTA = 0.5f;

        return new Temperature(
            this.deviceId,
            t + rnd.nextFloat(0, TEMP_DELTA),
            h + rnd.nextFloat(0, HUMIDITY_DELTA)
        );
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 75.0f);
    }
}
