package dev.nez.analytics.analyzer;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.nez.notification.Alert;
import dev.nez.notification.Alert.Severity;
import dev.nez.analytics.data.smoke.SmokeDetectorThresholds;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SmokeDetectorAnalyzer {

    public List<Alert> checkThreshold(SmokeDetectorData event, SmokeDetectorThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();
        final long dId = event.getDeviceId();
        final var ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());

        if (event.getSmokeRaw() < 0 || event.getCoLevel() < 0) {
            alerts.add(createAlert(dId, "smoke_system", event.getSmokeRaw(), Severity.FAULT, "SENSOR FAULT | Показники диму/CO не можуть бути від'ємними", ts));
            return alerts;
        }

        // Дим
        if (event.getSmokeRaw() > thresholds.maxSmokeRaw()) {
            alerts.add(createAlert(dId, "smoke", (float) event.getSmokeRaw(), Severity.CRITICAL, String.format("Виявлено дим! (Макс: %d)", thresholds.maxSmokeRaw()), ts));
        }

        // Чадний газ (CO)
        if (event.getCoLevel() > thresholds.maxCoLevel()) {
            alerts.add(createAlert(dId, "co_level", (float) event.getCoLevel(), Severity.CRITICAL, String.format("Небезпечний рівень чадного газу (Макс: %d)", thresholds.maxCoLevel()), ts));
        }

        return alerts;
    }

    private Alert createAlert(long dId, String metric, float val, Severity sev, String msg, Instant ts) {
        return new Alert(UuidCreator.getTimeOrderedEpoch(), dId, metric, val, sev, msg, ts);
    }
}