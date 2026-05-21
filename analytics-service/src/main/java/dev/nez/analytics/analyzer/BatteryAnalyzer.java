package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.battery.BatteryThresholds;
import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;

@Singleton
public class BatteryAnalyzer {

    @Nullable
    public Alert checkThreshold(
        BatteryData event,
        BatteryThresholds thresholds
    ) {
        final String OUT_OF_RANGE_MSG = "The result is outside the expected range";
        final String SENSOR_FAULT_MSG = "ERROR | SENSOR FAULT";

        final long deviceId = event.getDeviceId();
        final float val = event.getVal();

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        String msg = null;

        if (val < 0) {
            msg = SENSOR_FAULT_MSG;
        } else if (val < thresholds.minBatteryLevel()) {
            msg = OUT_OF_RANGE_MSG;
        }

        return msg != null
            ? new Alert(deviceId, msg, instant)
            : null;
    }
}
