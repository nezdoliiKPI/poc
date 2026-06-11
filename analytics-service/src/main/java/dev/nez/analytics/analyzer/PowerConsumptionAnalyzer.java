package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.alert.Alert;
import dev.nez.analytics.data.alert.Alert.Severity;
import dev.nez.analytics.data.power.PowerThresholds;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class PowerConsumptionAnalyzer {

    public List<Alert> checkThreshold(PowerConsumptionData event, PowerThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();

        final long dId = event.getDeviceId();
        final var ts = ProtoUtils.toInstant(event.getTimestamp());

        // Перевірка на помилку сенсора
        if (event.getPower() < 0 || event.getCurrent() < 0 || event.getVoltage() < 0) {
            alerts.add(Alert.createAlert(dId, "power_system", event.getPower(), null, null, Severity.FAULT, "SENSOR FAULT | Електричні показники не можуть бути від'ємними", ts));
            Log.warnf("Sensor fault on device %d: %s", dId, event.toString());
            return alerts;
        }

        // Напруга
        if (event.getVoltage() > thresholds.maxVoltage()) {
            alerts.add(Alert.createAlert(dId, "voltage", event.getVoltage(), thresholds.minVoltage(), thresholds.maxVoltage(), Severity.CRITICAL, String.format("Перенапруга (Макс: %.2fV)", thresholds.maxVoltage()), ts));
        } else if (event.getVoltage() < thresholds.minVoltage()) {
            alerts.add(Alert.createAlert(dId, "voltage", event.getVoltage(), thresholds.minVoltage(), thresholds.maxVoltage(), Severity.CRITICAL, String.format("Низька напруга (Мін: %.2fV)", thresholds.minVoltage()), ts));
        }

        // Струм
        if (event.getCurrent() > thresholds.maxCurrent()) {
            alerts.add(Alert.createAlert(dId, "current", event.getCurrent(), null, thresholds.maxCurrent(), Severity.CRITICAL, String.format("Перевищення струму (Макс: %.2fA)", thresholds.maxCurrent()), ts));
        }

        // Потужність
        if (event.getPower() > thresholds.maxPower()) {
            alerts.add(Alert.createAlert(dId, "power", event.getPower(), null, thresholds.maxPower(), Severity.WARNING, String.format("Перевищення потужності (Макс: %.2fW)", thresholds.maxPower()), ts));
        }

        return alerts;
    }
}