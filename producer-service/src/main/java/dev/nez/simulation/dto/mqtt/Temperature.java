package dev.nez.simulation.dto.mqtt;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Temperature(
    Long deviceId,
    Double temp
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return TemperatureMessage.newBuilder()
                .setDeviceId(deviceId)
                .setTemp(temp)
                .build()
                .toByteArray();
    }
}
