package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.event.PowerThresholds;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class PowerConsumptionAnalyzer {

    @Nullable
    public String checkThreshold(PowerConsumptionData event, PowerThresholds thresholds) {

        final long deviceId = event.getDeviceId();
        final float power = event.getPower();
        final float voltage = event.getVoltage();
        final float current = event.getCurrent();

        if (power < 0 || current < 0 || voltage < 0 || power > (thresholds.maxPower() * 10)) {
            return String.format("<b>ERROR | SENSOR FAULT</b>\nDev: <code>%d</code>", deviceId);
        }

        List<String> violations = new ArrayList<>();

        if (voltage > thresholds.maxVoltage()) {
            violations.add(String.format("V: %.1f (> %.1f)", voltage, thresholds.maxVoltage()));
        } else if (voltage < thresholds.minVoltage()) {
            violations.add(String.format("V: %.1f (< %.1f)", voltage, thresholds.minVoltage()));
        }

        if (current > thresholds.maxCurrent()) {
            violations.add(String.format("A: %.1f (> %.1f)", current, thresholds.maxCurrent()));
        }

        if (power > thresholds.maxPower()) {
            violations.add(String.format("kW: %.2f (> %.2f)", power, thresholds.maxPower()));
        }

        if (!violations.isEmpty()) {
            StringBuilder alert = new StringBuilder();
            alert.append(String.format("<b>ALERT | Dev: %d</b>\n", deviceId));

            for (String v : violations) {
                alert.append("- ").append(v).append("\n");
            }

            return alert.toString().trim();
        }

        return null;
    }
}
