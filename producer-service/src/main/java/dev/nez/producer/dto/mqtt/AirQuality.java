package dev.nez.producer.dto.mqtt;

import dev.nez.producer.dto.ProtocolBuffer;
import dev.nez.simulation.dto.mqtt.AirQualityMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Air Quality message body
 *
 * @param id    unique device ID
 * @param co2   CO2 concentration in ppm
 * @param pm25  PM2.5 particles in µg/m³
 * @param pm10  PM10 particles in µg/m³
 * @param tvoc  Total Volatile Organic Compounds in mg/m³
 * @param t     ambient t in Celsius
 * @param h     relative h percentage (0.0 to 100.0)
 */
@RegisterForReflection
public record AirQuality(
    Long id,
    Integer co2,
    Float pm25,
    Float pm10,
    Float tvoc,
    Float t,
    Float h
) implements ProtocolBuffer {

    @Override
    public byte[] serialize() {
        return AirQualityMessage.newBuilder()
                .setDeviceId(id)
                .setCo2(co2)
                .setPm25(pm25)
                .setPm10(pm10)
                .setTvoc(tvoc)
                .setTemperature(t)
                .setHumidity(h)
                .build()
                .toByteArray();
    }
}