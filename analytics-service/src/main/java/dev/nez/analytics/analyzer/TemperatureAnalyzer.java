package dev.nez.analytics.analyzer;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.nez.notification.Alert;
import dev.nez.notification.Alert.Severity;
import dev.nez.analytics.data.temperature.TemperatureThresholds;
import dev.nez.dto.proto.timeddata.TemperatureData;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class TemperatureAnalyzer {

    public List<Alert> checkThreshold(TemperatureData event, TemperatureThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();
        final long dId = event.getDeviceId();
        final var ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());

        // Temperature
        if (event.getTemperature() < -100 || event.getTemperature() > 150) {
            alerts.add(createAlert(dId, "temperature", event.getTemperature(), Severity.FAULT, "SENSOR FAULT | Температура поза фізичними межами", ts));
        } else if (event.getTemperature() > thresholds.maxTemperature()) {
            alerts.add(createAlert(dId, "temperature", event.getTemperature(), Severity.CRITICAL, String.format("Висока температура (Макс: %.2f)", thresholds.maxTemperature()), ts));
        } else if (event.getTemperature() < thresholds.minTemperature()) {
            alerts.add(createAlert(dId, "temperature", event.getTemperature(), Severity.CRITICAL, String.format("Низька температура (Мін: %.2f)", thresholds.minTemperature()), ts));
        }

        // Humidity
        if (event.getHumidity() < 0 || event.getHumidity() > 100) {
            alerts.add(createAlert(dId, "humidity", event.getHumidity(), Severity.FAULT, "SENSOR FAULT | Вологість поза фізичними межами (0-100)", ts));
        } else if (event.getHumidity() > thresholds.maxHumidity()) {
            alerts.add(createAlert(dId, "humidity", event.getHumidity(), Severity.WARNING, String.format("Висока вологість (Макс: %.2f)", thresholds.maxHumidity()), ts));
        } else if (event.getHumidity() < thresholds.minHumidity()) {
            alerts.add(createAlert(dId, "humidity", event.getHumidity(), Severity.WARNING, String.format("Низька вологість (Мін: %.2f)", thresholds.minHumidity()), ts));
        }

        return alerts;
    }

    private Alert createAlert(long dId, String metric, float val, Severity sev, String msg, Instant ts) {
        return new Alert(UuidCreator.getTimeOrderedEpoch(), dId, metric, val, sev, msg, ts);
    }
}