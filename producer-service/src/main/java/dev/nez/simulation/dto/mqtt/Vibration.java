package dev.nez.simulation.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * 3-Axis Vibration message body
 *
 * @param id unique device ID
 * @param x  acceleration on X-axis (g)
 * @param y  acceleration on Y-axis (g)
 * @param z  acceleration on Z-axis (g)
 *
 */
@RegisterForReflection
public record Vibration(
    Long id,
    Float x,
    Float y,
    Float z
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return VibrationMessage.newBuilder()
                .setDeviceId(id)
                .setX(x)
                .setY(y)
                .setZ(z)
                .build()
                .toByteArray();
    }
}
