package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.alert.Alert;
import dev.nez.analytics.data.alert.Alert.Severity;
import dev.nez.analytics.data.smoke.SmokeDetectorThresholds;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class SmokeDetectorAnalyzer {

    public List<Alert> checkThreshold(SmokeDetectorData event, SmokeDetectorThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();
        final long dId = event.getDeviceId();
        final var ts = ProtoUtils.toInstant(event.getTimestamp());

        if (event.getSmokeRaw() < 0 || event.getCoLevel() < 0) {
            alerts.add(Alert.createAlert(dId, "smoke_system", (float) event.getSmokeRaw(), null, null, Severity.FAULT, "SENSOR FAULT | Показники диму/CO не можуть бути від'ємними", ts));
            return alerts;
        }

        // Дим
        if (event.getSmokeRaw() > thresholds.maxSmokeRaw()) {
            alerts.add(Alert.createAlert(dId, "smoke", (float) event.getSmokeRaw(), null, (float) thresholds.maxSmokeRaw(), Severity.CRITICAL, String.format("Виявлено дим! (Макс: %d)", thresholds.maxSmokeRaw()), ts));
        }

        // Чадний газ (CO)
        if (event.getCoLevel() > thresholds.maxCoLevel()) {
            alerts.add(Alert.createAlert(dId, "co_level", (float) event.getCoLevel(), null, (float) thresholds.maxCoLevel(), Severity.CRITICAL, String.format("Небезпечний рівень чадного газу (Макс: %d)", thresholds.maxCoLevel()), ts));
        }

        return alerts;
    }
}