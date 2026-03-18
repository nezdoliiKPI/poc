package dev.nez.producer.simulation.generator;

import dev.nez.producer.dto.mqtt.PowerConsumption;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

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
        final var device = new Device(hardwareId, password, topic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, 0, 1, 1);

        super(device, messageType, mainTiming);
    }

    @Override
    public Object getData() {
        final var cv = BASE_VOLTAGE + (random.nextFloat() * 2 * VOLTAGE_NOISE_MARGIN - VOLTAGE_NOISE_MARGIN);
        final var cf = 0.05f + random.nextFloat() * 1.95f;
        final var pow = cv * cf * (0.8f + random.nextFloat() * 0.2f);

        return new PowerConsumption(deviceId, cv, cf, pow);
    }
}
