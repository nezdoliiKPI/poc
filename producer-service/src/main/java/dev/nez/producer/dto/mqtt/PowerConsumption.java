package dev.nez.producer.dto.mqtt;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.simulation.dto.mqtt.PowerConsumptionMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Power Consumption message body
 *
 * @param id    unique device ID
 * @param cv    current voltage (V)
 * @param cf    current flow (A)
 * @param pow   active power (W)
 */
@RegisterForReflection
public record PowerConsumption(
    Long id,
    Float cv,
    Float cf,
    Float pow
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return PowerConsumptionMessage.newBuilder()
                .setDeviceId(id)
                .setVoltage(cv)
                .setCurrent(cf)
                .setPower(pow)
                .build()
                .toByteArray();
    }
}
