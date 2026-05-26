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
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(2), 1, 1);
        final var batteryTiming = new MessageTiming(TimeUnit.MINUTES, 0, 1, 60);

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

        final int CO2_DELTA = 5;
        final float PM25_DELTA = 1.0f;
        final float PM10_DELTA = 1.0f;
        final float TVOC_DELTA = 0.1f;
        final float TEMP_DELTA = 0.5f;
        final float HUMIDITY_DELTA = 0.5f;

        return new AirQuality(
            this.deviceId,
            co2 + rnd.nextInt(CO2_DELTA),
            pm25 + rnd.nextFloat(PM25_DELTA),
            pm10 + rnd.nextFloat(PM10_DELTA),
            tvoc  + rnd.nextFloat(TVOC_DELTA),
            t + rnd.nextFloat(TEMP_DELTA),
            h + rnd.nextFloat(HUMIDITY_DELTA)
        );
    }

    @Override
    public Object getBatteryData() {
        return new Battery(this.deviceId, 75.0f);
    }
}