package dev.nez.producer.simulation.generator.data;

import dev.nez.producer.dto.mqtt.PowerConsumption;
import dev.nez.producer.simulation.model.Device;
import dev.nez.producer.simulation.model.MessageTiming;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class PowerDataGenerator extends DeviceDataGenerator {

    public PowerDataGenerator(
        String hardwareId,
        String password,
        String topic,
        MessageType messageType
    ) {
        final Random rnd = ThreadLocalRandom.current();

        final var device = new Device(hardwareId, password, topic);
        final var mainTiming = new MessageTiming(TimeUnit.SECONDS, rnd.nextInt(2), 1, 1);

        super(device, messageType, mainTiming);
    }

    @Override
    public Object getData() {
        final var cv = 230.0f;
        final var cf = 1.0f;
        final var pf = 0.9f;

        return new PowerConsumption(
            this.deviceId,
            cv + rnd.nextFloat(-1.0f, 1.0f),
            cf  + rnd.nextFloat(-0.95f, 1.0f),
            cv * cf * pf
        );
    }
}
