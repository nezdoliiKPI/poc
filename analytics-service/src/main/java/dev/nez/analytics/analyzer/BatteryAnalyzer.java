package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.alert.Alert;
import dev.nez.analytics.data.alert.Alert.Severity;
import dev.nez.analytics.data.battery.BatteryThresholds;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.BatteryData;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class BatteryAnalyzer {

    public List<Alert> checkThreshold(BatteryData event, BatteryThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();

        final long dId = event.getDeviceId();
        final var ts = ProtoUtils.toInstant(event.getTimestamp());
        final float val = event.getVal();

        if (val < 0) {
            alerts.add(Alert.createAlert(dId, "battery", val, null, null, Severity.FAULT, "SENSOR FAULT | Заряд не може бути від'ємним", ts));
        } else if (val < thresholds.minBatteryLevel()) {
            alerts.add(Alert.createAlert(dId, "battery", val, thresholds.minBatteryLevel(), null, Severity.WARNING, String.format("Низький заряд батареї (Мін: %.2f%%)", thresholds.minBatteryLevel()), ts));
        }

        return alerts;
    }
}