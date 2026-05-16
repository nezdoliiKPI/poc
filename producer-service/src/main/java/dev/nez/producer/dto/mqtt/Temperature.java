package dev.nez.producer.dto.mqtt;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.dto.proto.mqtt.TemperatureMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Represents a single reading of temperature (and optional humidity) telemetry from a device.
 *
 * @param id          The unique identifier of the sensor or device.
 * @param t The ambient temperature in degrees Celsius (°C).
 * @param h    The relative humidity percentage (%).
 */
@RegisterForReflection
public record Temperature(
    Long id,
    Float t,
    Float h
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return TemperatureMessage.newBuilder()
                .setDeviceId(id)
                .setTemperature(t)
                .setHumidity(h)
                .build()
                .toByteArray();
    }
}
