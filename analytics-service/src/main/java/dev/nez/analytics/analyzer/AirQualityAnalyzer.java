package dev.nez.analytics.analyzer;

import dev.nez.analytics.data.alert.Alert;
import dev.nez.analytics.data.alert.Alert.Severity;
import dev.nez.analytics.data.air.AirQualityThresholds;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.AirQualityData;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AirQualityAnalyzer {

    public List<Alert> checkThreshold(AirQualityData event, AirQualityThresholds thresholds) {
        List<Alert> alerts = new ArrayList<>();

        final long dId = event.getDeviceId();
        final var ts = ProtoUtils.toInstant(event.getTimestamp());

        // CO2
        if (event.getCo2() < 0) {
            alerts.add(Alert.createAlert(dId, "co2", (float) event.getCo2(), null, null, Severity.FAULT, "SENSOR FAULT | Значення не може бути від'ємним", ts));
        } else if (event.getCo2() > thresholds.maxCo2()) {
            alerts.add(Alert.createAlert(dId, "co2", (float) event.getCo2(), null, (float) thresholds.maxCo2(), Severity.CRITICAL, String.format("Перевищення CO2 (Макс: %d)", thresholds.maxCo2()), ts));
        }

        // PM2.5
        if (event.getPm25() < 0) {
            alerts.add(Alert.createAlert(dId, "pm25", event.getPm25(), null, null, Severity.FAULT, "SENSOR FAULT | Значення не може бути від'ємним", ts));
        } else if (event.getPm25() > thresholds.maxPm25()) {
            alerts.add(Alert.createAlert(dId, "pm25", event.getPm25(), null, thresholds.maxPm25(), Severity.CRITICAL, String.format("Перевищення PM2.5 (Макс: %.2f)", thresholds.maxPm25()), ts));
        }

        // PM10
        if (event.getPm10() < 0) {
            alerts.add(Alert.createAlert(dId, "pm10", event.getPm10(), null, null, Severity.FAULT, "SENSOR FAULT | Значення не може бути від'ємним", ts));
        } else if (event.getPm10() > thresholds.maxPm10()) {
            alerts.add(Alert.createAlert(dId, "pm10", event.getPm10(), null, thresholds.maxPm10(), Severity.CRITICAL, String.format("Перевищення PM10 (Макс: %.2f)", thresholds.maxPm10()), ts));
        }

        // TVOC
        if (event.getTvoc() < 0) {
            alerts.add(Alert.createAlert(dId, "tvoc", event.getTvoc(), null, null, Severity.FAULT, "SENSOR FAULT | Значення не може бути від'ємним", ts));
        } else if (event.getTvoc() > thresholds.maxTvoc()) {
            alerts.add(Alert.createAlert(dId, "tvoc", event.getTvoc(), null, thresholds.maxTvoc(), Severity.WARNING, String.format("Перевищення TVOC (Макс: %.2f)", thresholds.maxTvoc()), ts));
        }

        // Temperature
        if (event.getTemperature() < -100 || event.getTemperature() > 150) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), null, null, Severity.FAULT, "SENSOR FAULT | Значення поза фізичними межами", ts));
        } else if (event.getTemperature() > thresholds.maxTemperature()) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), thresholds.minTemperature(), thresholds.maxTemperature(), Severity.CRITICAL, String.format("Висока температура (Макс: %.2f)", thresholds.maxTemperature()), ts));
        } else if (event.getTemperature() < thresholds.minTemperature()) {
            alerts.add(Alert.createAlert(dId, "temperature", event.getTemperature(), thresholds.minTemperature(), thresholds.maxTemperature(), Severity.CRITICAL, String.format("Низька температура (Мін: %.2f)", thresholds.minTemperature()), ts));
        }

        // Humidity
        if (event.getHumidity() < 0 || event.getHumidity() > 100) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), null, null, Severity.FAULT, "SENSOR FAULT | Значення поза фізичними межами (0-100)", ts));
        } else if (event.getHumidity() > thresholds.maxHumidity()) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), thresholds.minHumidity(), thresholds.maxHumidity(), Severity.WARNING, String.format("Висока вологість (Макс: %.2f)", thresholds.maxHumidity()), ts));
        } else if (event.getHumidity() < thresholds.minHumidity()) {
            alerts.add(Alert.createAlert(dId, "humidity", event.getHumidity(), thresholds.minHumidity(), thresholds.maxHumidity(), Severity.WARNING, String.format("Низька вологість (Мін: %.2f)", thresholds.minHumidity()), ts));
        }

        return alerts;
    }
}