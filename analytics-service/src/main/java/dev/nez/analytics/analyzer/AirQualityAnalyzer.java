package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.air.AirQualityThresholds;
import dev.nez.dto.proto.timeddata.AirQualityData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
public class AirQualityAnalyzer {

    @Nullable
    public Alert checkThreshold(
        AirQualityData event,
        AirQualityThresholds thresholds
    ) {
        final String OUT_OF_RANGE_MSG = "The result is outside the expected range";
        final String SENSOR_FAULT_MSG = "ERROR | SENSOR FAULT";

        final long deviceId = event.getDeviceId();
        final int co2 = event.getCo2();
        final float pm25 = event.getPm25();
        final float pm10 = event.getPm10();
        final float tvoc = event.getTvoc();
        final float temp = event.getTemperature();
        final float hum = event.getHumidity();

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        String msg = null;

        if (co2 < 0 || pm25 < 0 || pm10 < 0 || tvoc < 0 || hum < 0 || hum > 100 || temp < -100 || temp > 150) {
            msg = SENSOR_FAULT_MSG;
        }
        else if (
            co2 > thresholds.maxCo2() ||
                pm25 > thresholds.maxPm25() ||
                pm10 > thresholds.maxPm10() ||
                tvoc > thresholds.maxTvoc() ||
                temp > thresholds.maxTemperature() ||
                temp < thresholds.minTemperature() ||
                hum > thresholds.maxHumidity() ||
                hum < thresholds.minHumidity()
        ) {
            msg = OUT_OF_RANGE_MSG;
        }

        return msg != null
            ? new Alert(deviceId, msg, instant)
            : null;
    }
}