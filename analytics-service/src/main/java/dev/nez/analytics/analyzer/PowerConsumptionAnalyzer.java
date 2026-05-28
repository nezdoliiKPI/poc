package dev.nez.analytics.analyzer;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.nez.notification.Alert;
import dev.nez.notification.Alert.Severity;
import dev.nez.analytics.data.power.PowerThresholds;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PowerConsumptionAnalyzer {

    public List<Alert> checkThreshold(PowerConsumptionData event, PowerThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();
        final long dId = event.getDeviceId();
        final var ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());

        // Перевірка на помилку сенсора
        if (event.getPower() < 0 || event.getCurrent() < 0 || event.getVoltage() < 0) {
            alerts.add(createAlert(dId, "power_system", event.getPower(), Severity.FAULT, "SENSOR FAULT | Електричні показники не можуть бути від'ємними", ts));
            Log.warnf("Sensor fault on device %d: %s", dId, event.toString());
            return alerts; // Перериваємо перевірку інших порогів для цього пакета
        }

        // Напруга
        if (event.getVoltage() > thresholds.maxVoltage()) {
            alerts.add(createAlert(dId, "voltage", event.getVoltage(), Severity.CRITICAL, String.format("Перенапруга (Макс: %.2fV)", thresholds.maxVoltage()), ts));
        } else if (event.getVoltage() < thresholds.minVoltage()) {
            alerts.add(createAlert(dId, "voltage", event.getVoltage(), Severity.CRITICAL, String.format("Низька напруга (Мін: %.2fV)", thresholds.minVoltage()), ts));
        }

        // Струм
        if (event.getCurrent() > thresholds.maxCurrent()) {
            alerts.add(createAlert(dId, "current", event.getCurrent(), Severity.CRITICAL, String.format("Перевищення струму (Макс: %.2fA)", thresholds.maxCurrent()), ts));
        }

        // Потужність
        if (event.getPower() > thresholds.maxPower()) {
            alerts.add(createAlert(dId, "power", event.getPower(), Severity.WARNING, String.format("Перевищення потужності (Макс: %.2fW)", thresholds.maxPower()), ts));
        }

        return alerts;
    }

    private Alert createAlert(long dId, String metric, float val, Severity sev, String msg, Instant ts) {
        return new Alert(UuidCreator.getTimeOrderedEpoch(), dId, metric, val, sev, msg, ts);
    }
}