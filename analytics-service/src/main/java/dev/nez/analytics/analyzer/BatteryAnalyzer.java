package dev.nez.analytics.analyzer;

import dev.nez.alert.Alert;
import dev.nez.analytics.data.battery.BatteryThresholds;
import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class BatteryAnalyzer {

    @Nullable
    public Alert checkThreshold(
        BatteryData event,
        BatteryThresholds thresholds
    ) {
        final long deviceId = event.getDeviceId();
        final float val = event.getVal();

        final ArrayList<String> messages = new ArrayList<>();

        if (val < 0) {
            messages.add(String.format("<b>ERROR | SENSOR FAULT</b>\nDev: <code>%d</code>", deviceId));
        } else {
            if (val < thresholds.minBatteryLevel()) {
                messages.add(String.format("BATT: %.1f (< %.1f)", val, thresholds.minBatteryLevel()));
            }
        }

        if (messages.isEmpty()) {
            return null;
        }

        return new Alert(deviceId, messages);
    }
}
