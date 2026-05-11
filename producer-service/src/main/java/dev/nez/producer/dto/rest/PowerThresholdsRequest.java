package dev.nez.producer.dto.rest;

public record PowerThresholdsRequest(
    Long deviceId,
    Float minVoltage,
    Float maxVoltage,
    Float maxCurrent,
    Float maxPower
) {
}