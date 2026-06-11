package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.alert.Alert;
import dev.nez.analytics.data.alert.Alert.Severity;
import dev.nez.analytics.data.temperature.TemperatureThresholds;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.TemperatureData;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class TemperatureAnalyzer {

    public List<Alert> checkThreshold(TemperatureData event, TemperatureThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();

        final long dId = event.getDeviceId();
        final var ts = ProtoUtils.toInstant(event.getTimestamp());

        // Temperature
        if (event.getTemperature() < -100 || event.getTemperature() > 1000) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), null, null, Severity.FAULT, "SENSOR FAULT | Температура поза фізичними межами", ts));
        } else if (event.getTemperature() > thresholds.maxTemperature()) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), thresholds.minTemperature(), thresholds.maxTemperature(), Severity.CRITICAL, String.format("Висока температура (Макс: %.2f)", thresholds.maxTemperature()), ts));
        } else if (event.getTemperature() < thresholds.minTemperature()) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), thresholds.minTemperature(), thresholds.maxTemperature(), Severity.CRITICAL, String.format("Низька температура (Мін: %.2f)", thresholds.minTemperature()), ts));
        }

        // Humidity
        if (event.getHumidity() < 0 || event.getHumidity() > 100) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), null, null, Severity.FAULT, "SENSOR FAULT | Вологість поза фізичними межами (0-100)", ts));
        } else if (event.getHumidity() > thresholds.maxHumidity()) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), thresholds.minHumidity(), thresholds.maxHumidity(), Severity.WARNING, String.format("Висока вологість (Макс: %.2f)", thresholds.maxHumidity()), ts));
        } else if (event.getHumidity() < thresholds.minHumidity()) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), thresholds.minHumidity(), thresholds.maxHumidity(), Severity.WARNING, String.format("Низька вологість (Мін: %.2f)", thresholds.minHumidity()), ts));
        }

        return alerts;
    }
}