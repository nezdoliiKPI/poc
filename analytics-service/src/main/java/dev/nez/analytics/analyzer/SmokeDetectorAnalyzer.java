package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.smoke.SmokeDetectorThresholds;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
public class SmokeDetectorAnalyzer {

    @Nullable
    public Alert checkThreshold(
        SmokeDetectorData event,
        SmokeDetectorThresholds thresholds
    ) {
        final String OUT_OF_RANGE_MSG = "The result is outside the expected range";
        final String SENSOR_FAULT_MSG = "ERROR | SENSOR FAULT";

        final long deviceId = event.getDeviceId();
        final int smokeRaw = event.getSmokeRaw();
        final int coLevel = event.getCoLevel();

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        String msg = null;

        if (smokeRaw < 0 || coLevel < 0) {
            msg = SENSOR_FAULT_MSG;
        } else if (
            smokeRaw > thresholds.maxSmokeRaw() ||
                coLevel > thresholds.maxCoLevel()
        ) {
            msg = OUT_OF_RANGE_MSG;
        }

        return msg != null
            ? new Alert(deviceId, msg, instant)
            : null;
    }
}