package dev.nez.producer.dto.mqtt;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.proto.mqtt.BatteryMessage;
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
