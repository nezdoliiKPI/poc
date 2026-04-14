package dev.nez.producer.simulation.generator;

import dev.nez.producer.dto.mqtt.AirQuality;
import dev.nez.producer.dto.mqtt.Battery;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AirDataGenerator extends DeviceDataGenerator{

    public AirDataGenerator(
        String hardwareId,
        String password,
        String topic,
        String batteryTopic,
        MessageType messageType
    ) {
        final Random rnd = ThreadLocalRandom.current();

        final var device = new Device(hardwareId, password, topic, batteryTopic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(10), 60, 60);
        final var batteryTiming = new MessageTiming(TimeUnit.MINUTES, 0, 30, 30);

        super(device, messageType, mainTiming, batteryTiming);
    }

    @Override
    public Object getData() {
        final var co2 = 350 + rnd.nextInt(600);
        final var pm25 = rnd.nextFloat() * 15.0f;
        final var pm10 = rnd.nextFloat() * 30.0f;
        final var tvoc = rnd.nextFloat() * 0.5f;
        final var t = 25.0f + (rnd.nextFloat() * 5.0f);
        final var h = 45.0f + (rnd.nextFloat() * 20.0f);

        return new AirQuality(this.deviceId, co2, pm25, pm10, tvoc, t, h);
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 99.0f);
    }
}