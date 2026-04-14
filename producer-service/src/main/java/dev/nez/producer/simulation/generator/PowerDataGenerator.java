package dev.nez.producer.simulation.generator;

import dev.nez.producer.dto.mqtt.PowerConsumption;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class PowerDataGenerator extends DeviceDataGenerator{
    private static final float BASE_VOLTAGE = 230.0f;
    private static final float VOLTAGE_NOISE_MARGIN = 10.0f;

    public PowerDataGenerator(
        String hardwareId,
        String password,
        String topic,
        MessageType messageType
    ) {
        final Random rnd = ThreadLocalRandom.current();

        final var device = new Device(hardwareId, password, topic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(10), 1, 1);

        super(device, messageType, mainTiming);
    }

    @Override
    public Object getData() {
        final var cv = BASE_VOLTAGE + (rnd.nextFloat() * 2 * VOLTAGE_NOISE_MARGIN - VOLTAGE_NOISE_MARGIN);
        final var cf = 0.05f + rnd.nextFloat() * 1.95f;
        final var pow = cv * cf * (0.8f + rnd.nextFloat() * 0.2f);

        return new PowerConsumption(deviceId, cv, cf, pow);
    }
}
