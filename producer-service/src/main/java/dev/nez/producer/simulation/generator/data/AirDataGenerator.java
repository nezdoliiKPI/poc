package dev.nez.producer.simulation.generator.data;

import dev.nez.producer.dto.mqtt.AirQuality;
import dev.nez.producer.dto.mqtt.Battery;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AirDataGenerator extends DeviceDataGenerator {

    public AirDataGenerator(
        String hardwareId,
        String password,
        String topic,
        String batteryTopic,
        MessageType messageType
    ) {
        final Random rnd = ThreadLocalRandom.current();

        final var device = new Device(hardwareId, password, topic, batteryTopic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(2), 60, 60);
        final var batteryTiming = new MessageTiming(TimeUnit.MINUTES, 0, 30, 30);

        super(device, messageType, mainTiming, batteryTiming);
    }

    @Override
    public Object getData() {
        final var co2 = 450;
        final var pm25 = 5.0f;
        final var pm10 = 10.0f;
        final var tvoc = 0.1f;
        final var t = 23.0f;
        final var h = 45.0f;

        return new AirQuality(
            this.deviceId,
            co2 + rnd.nextInt(-6, 7),
            pm25 + rnd.nextFloat(-1.1f, 1.1f),
            pm10 + rnd.nextFloat(-1.1f, 1.1f),
            tvoc  + rnd.nextFloat(-0.2f, 0.2f),
            t + rnd.nextFloat(-0.5f, 0.5f),
            h + rnd.nextFloat(-0.5f, 0.5f)
        );
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 99.0f);
    }
}