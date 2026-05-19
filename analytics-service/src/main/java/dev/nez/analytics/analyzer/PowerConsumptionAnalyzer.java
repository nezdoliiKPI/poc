package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.power.PowerThresholds;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import io.quarkus.logging.Log;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;

@Singleton
public class PowerConsumptionAnalyzer {

    @Nullable
    public Alert checkThreshold(
        PowerConsumptionData event,
        PowerThresholds thresholds
    ) {
        final long deviceId = event.getDeviceId();
        final float power = event.getPower();
        final float voltage = event.getVoltage();
        final float current = event.getCurrent();

        final ArrayList<String> messages = new ArrayList<>();

        if (power < 0 || current < 0 || voltage < 0) {
            messages.add("ERROR | SENSOR FAULT");
            Log.warnf(event.toString());
        } else {
            if (voltage > thresholds.maxVoltage()) {
                messages.add(String.format("V: %.1f (> %.1f)", voltage, thresholds.maxVoltage()));
            } else if (voltage < thresholds.minVoltage()) {
                messages.add(String.format("V: %.1f (< %.1f)", voltage, thresholds.minVoltage()));
            }

            if (current > thresholds.maxCurrent()) {
                messages.add(String.format("A: %.1f (> %.1f)", current, thresholds.maxCurrent()));
            }
            if (power > thresholds.maxPower()) {
                messages.add(String.format("W: %.2f (> %.2f)", power, thresholds.maxPower()));
            }
        }

        if (messages.isEmpty()) {
            return null;
        }

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        return new Alert(deviceId, messages, instant);
    }
}
