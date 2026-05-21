package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.temperature.TemperatureThresholds;
import dev.nez.dto.proto.timeddata.TemperatureData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
public class TemperatureAnalyzer {

    @Nullable
    public Alert checkThreshold(
        TemperatureData event,
        TemperatureThresholds thresholds
    ) {
        final String OUT_OF_RANGE_MSG = "The result is outside the expected range";
        final String SENSOR_FAULT_MSG = "ERROR | SENSOR FAULT";

        final long deviceId = event.getDeviceId();
        final float temp = event.getTemperature();
        final float hum = event.getHumidity();

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        String msg = null;

        if (hum < 0 || hum > 100 || temp < -100 || temp > 150) {
            msg = SENSOR_FAULT_MSG;
        } else if (
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