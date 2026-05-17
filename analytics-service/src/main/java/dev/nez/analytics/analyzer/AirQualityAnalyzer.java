package dev.nez.analytics.analyzer;

import dev.nez.alert.Alert;
import dev.nez.analytics.data.air.AirQualityThresholds;
import dev.nez.dto.proto.timeddata.AirQualityData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class AirQualityAnalyzer {

    @Nullable
    public Alert checkThreshold(
        AirQualityData event,
        AirQualityThresholds thresholds
    ) {
        final long deviceId = event.getDeviceId();
        final int co2 = event.getCo2();
        final float pm25 = event.getPm25();
        final float pm10 = event.getPm10();
        final float tvoc = event.getTvoc();
        final float temp = event.getTemperature();
        final float hum = event.getHumidity();

        final ArrayList<String> messages = new ArrayList<>();

        if (co2 < 0 || pm25 < 0 || pm10 < 0 || tvoc < 0 || hum < 0 || hum > 100 || temp < -100 || temp > 150) {
            messages.add(String.format("<b>ERROR | SENSOR FAULT</b>\nDev: <code>%d</code>", deviceId));
        } else {
            if (co2 > thresholds.maxCo2()) {
                messages.add(String.format("CO2: %d (> %d)", co2, thresholds.maxCo2()));
            }
            if (pm25 > thresholds.maxPm25()) {
                messages.add(String.format("PM2.5: %.1f (> %.1f)", pm25, thresholds.maxPm25()));
            }
            if (pm10 > thresholds.maxPm10()) {
                messages.add(String.format("PM10: %.1f (> %.1f)", pm10, thresholds.maxPm10()));
            }
            if (tvoc > thresholds.maxTvoc()) {
                messages.add(String.format("TVOC: %.3f (> %.3f)", tvoc, thresholds.maxTvoc()));
            }

            if (temp > thresholds.maxTemperature()) {
                messages.add(String.format("T: %.1f°C (> %.1f°C)", temp, thresholds.maxTemperature()));
            } else if (temp < thresholds.minTemperature()) {
                messages.add(String.format("T: %.1f°C (< %.1f°C)", temp, thresholds.minTemperature()));
            }

            if (hum > thresholds.maxHumidity()) {
                messages.add(String.format("H: %.1f%% (> %.1f%%)", hum, thresholds.maxHumidity()));
            } else if (hum < thresholds.minHumidity()) {
                messages.add(String.format("H: %.1f%% (< %.1f%%)", hum, thresholds.minHumidity()));
            }
        }

        if (messages.isEmpty()) {
            return null;
        }

        return new Alert(deviceId, messages);
    }
}
