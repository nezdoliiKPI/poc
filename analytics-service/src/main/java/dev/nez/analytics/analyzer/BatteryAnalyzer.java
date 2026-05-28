package dev.nez.analytics.analyzer;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.nez.notification.Alert;
import dev.nez.notification.Alert.Severity;
import dev.nez.analytics.data.battery.BatteryThresholds;
import dev.nez.dto.proto.timeddata.BatteryData;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BatteryAnalyzer {

    public List<Alert> checkThreshold(BatteryData event, BatteryThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();
        final long dId = event.getDeviceId();
        final var ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());
        final float val = event.getVal();

        if (val < 0) {
            alerts.add(createAlert(dId, "battery", val, Severity.FAULT, "SENSOR FAULT | Заряд не може бути від'ємним", ts));
        } else if (val < thresholds.minBatteryLevel()) {
            alerts.add(createAlert(dId, "val", val, Severity.WARNING, String.format("Низький заряд батареї (Мін: %.2f%%)", thresholds.minBatteryLevel()), ts));
        }

        return alerts;
    }

    private Alert createAlert(long dId, String metric, float val, Severity sev, String msg, Instant ts) {
        return new Alert(UuidCreator.getTimeOrderedEpoch(), dId, metric, val, sev, msg, ts);
    }
}