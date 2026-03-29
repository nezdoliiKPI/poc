package dev.nez.producer.dto.mqtt;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.simulation.dto.mqtt.SmokeDetectorMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Smoke detector message body
 *
 * @param id    unique device ID
 * @param sr    raw optical sensor value (0–1023 ADC)
 * @param co    CO concentration in ppm
 *
 */
@RegisterForReflection
public record SmokeDetector(
    Long id,
    Integer sr,
    Integer co
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return SmokeDetectorMessage.newBuilder()
                .setDeviceId(id)
                .setSmokeRaw(sr)
                .setCoLevel(co)
                .build()
                .toByteArray();
    }
}
