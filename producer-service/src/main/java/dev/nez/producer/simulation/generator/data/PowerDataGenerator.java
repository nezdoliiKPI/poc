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
        final var mainTiming = new MessageTiming(TimeUnit.MILLISECONDS, rnd.nextInt(1000), 500, 1);

        super(device, messageType, mainTiming);
    }

    @Override
    public Object getData() {
        final float VOLTAGE_DELTA = 1.0f;
        final float CURRENT_DELTA = 0.05f;

        final var cv = 230.0f + rnd.nextFloat(VOLTAGE_DELTA);
        final var cf = 1.0f   + rnd.nextFloat(CURRENT_DELTA);
        final var pf = 0.8f; //Power Factor

        return new PowerConsumption(
            this.deviceId,
            cv ,
            cf  ,
            cv * cf * pf
        );
    }
}
