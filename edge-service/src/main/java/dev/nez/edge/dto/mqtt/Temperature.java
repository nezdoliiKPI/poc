package dev.nez.edge.dto.mqtt;

public record Temperature(
    Long deviceId,
    Double temp
) {
}
