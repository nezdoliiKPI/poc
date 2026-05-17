package dev.nez.analytics.analyzer;

import dev.nez.alert.Alert;
import dev.nez.analytics.data.smoke.SmokeDetectorThresholds;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class SmokeDetectorAnalyzer {

    @Nullable
    public Alert checkThreshold(
        SmokeDetectorData event,
        SmokeDetectorThresholds thresholds
    ) {
        final long deviceId = event.getDeviceId();
        final int smokeRaw = event.getSmokeRaw();
        final int coLevel = event.getCoLevel();

        final ArrayList<String> messages = new ArrayList<>();

        if (smokeRaw < 0 || coLevel < 0) {
            messages.add(String.format("<b>ERROR | SENSOR FAULT</b>\nDev: <code>%d</code>", deviceId));
        } else {
            if (smokeRaw > thresholds.maxSmokeRaw()) {
                messages.add(String.format("SMOKE: %d (> %d)", smokeRaw, thresholds.maxSmokeRaw()));
            }
            if (coLevel > thresholds.maxCoLevel()) {
                messages.add(String.format("CO: %d (> %d)", coLevel, thresholds.maxCoLevel()));
            }
        }

        if (messages.isEmpty()) {
            return null;
        }

        return new Alert(deviceId, messages);
    }
}
