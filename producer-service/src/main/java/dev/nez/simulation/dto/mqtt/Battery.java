package dev.nez.simulation.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Battery message body
 *
 * @param id unique device ID
 * @param v
 */
@RegisterForReflection
public record Battery(
    Long id,
    Float v
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return BatteryMessage.newBuilder()
                .setDeviceId(id)
                .setVal(v)
                .build()
                .toByteArray();
    }
}
