package dev.nez.simulation.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Temperature and Humidity message body
 *
 * @param id unique device ID
 * @param t temperature Celsius
 * @param h humidity percentage (0.0 to 100.0)
 *
 */
@RegisterForReflection
public record TemperatureHumidity(
    Long id,
    Float t,
    Float h
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return TemperatureHumidityMessage.newBuilder()
                .setDeviceId(id)
                .setTemp(t)
                .setHumidity(h)
                .build()
                .toByteArray();
    }
}
