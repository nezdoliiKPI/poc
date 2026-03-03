package dev.nez.simulation.dto.mqtt;

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
