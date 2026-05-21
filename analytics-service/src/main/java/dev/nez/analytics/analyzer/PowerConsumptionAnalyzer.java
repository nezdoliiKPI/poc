package dev.nez.analytics.analyzer;

import dev.nez.notification.Alert;
import dev.nez.analytics.data.power.PowerThresholds;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;

import io.quarkus.logging.Log;
import io.smallrye.common.constraint.Nullable;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
public class PowerConsumptionAnalyzer {

    @Nullable
    public Alert checkThreshold(
        PowerConsumptionData event,
        PowerThresholds thresholds
    ) {
        final String OUT_OF_RANGE_MSG = "The result is outside the expected range";
        final String SENSOR_FAULT_MSG = "ERROR | SENSOR FAULT";

        final long deviceId = event.getDeviceId();
        final float power = event.getPower();
        final float voltage = event.getVoltage();
        final float current = event.getCurrent();

        final var instant = Instant.ofEpochSecond(
            event.getTimestamp().getSeconds(),
            event.getTimestamp().getNanos()
        );

        String msg = null;

        if (power < 0 || current < 0 || voltage < 0) {
            msg = SENSOR_FAULT_MSG;
            Log.warnf(event.toString());
        } else if (
            voltage > thresholds.maxVoltage() ||
                voltage < thresholds.minVoltage() ||
                current > thresholds.maxCurrent() ||
                power > thresholds.maxPower()
        ) {
            msg = OUT_OF_RANGE_MSG;
        }

        return msg != null
            ? new Alert(deviceId, msg, instant)
            : null;
    }
}
