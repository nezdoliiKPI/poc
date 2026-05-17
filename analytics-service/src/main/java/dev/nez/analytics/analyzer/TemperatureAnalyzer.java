package dev.nez.analytics.analyzer;

import dev.nez.alert.Alert;
import dev.nez.analytics.data.temperature.TemperatureThresholds;
import dev.nez.dto.proto.timeddata.TemperatureData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class TemperatureAnalyzer {

    @Nullable
    public Alert checkThreshold(
        TemperatureData event,
        TemperatureThresholds thresholds
    ) {
        final long deviceId = event.getDeviceId();
        final float temp = event.getTemperature();
        final float hum = event.getHumidity();

        final ArrayList<String> messages = new ArrayList<>();

        if (hum < 0 || hum > 100 || temp < -100 || temp > 150) {
            messages.add(String.format("<b>ERROR | SENSOR FAULT</b>\nDev: <code>%d</code>", deviceId));
        } else {
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
