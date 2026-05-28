package dev.nez.monitoring.consumer;

import dev.nez.monitoring.dto.*;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;

import java.util.Objects;

@ApplicationScoped
public class KafkaConsumer {
    private final BroadcastProcessor<PowerConsumptionPoint> powerProcessor =
        BroadcastProcessor.create();
    private final BroadcastProcessor<TemperaturePoint> temperatureProcessor =
        BroadcastProcessor.create();
    private final BroadcastProcessor<AirQualityPoint> airProcessor =
        BroadcastProcessor.create();
    private final BroadcastProcessor<BatteryPoint> batteryProcessor =
        BroadcastProcessor.create();
    private final BroadcastProcessor<SmokeDetectorPoint> smokeProcessor =
        BroadcastProcessor.create();
    private final BroadcastProcessor<Alert> alertProcessor =
        BroadcastProcessor.create();

    public KafkaConsumer(
        @Channel("power-consumption") Multi<PowerConsumptionPoint> power,
        @Channel("temperature")       Multi<TemperaturePoint> temperature,
        @Channel("air-quality")       Multi<AirQualityPoint> air,
        @Channel("battery")           Multi<BatteryPoint> battery,
        @Channel("smoke-detector")    Multi<SmokeDetectorPoint> smoke,
        @Channel("alert")             Multi<Alert> alert
    ) {
        power.filter(Objects::nonNull)
            .subscribe().with(
                powerProcessor::onNext,
                err -> Log.error("Power stream error", err));

        temperature.filter(Objects::nonNull)
            .subscribe().with(
                temperatureProcessor::onNext,
                err -> Log.error("Temperature stream error", err));

        air.filter(Objects::nonNull)
            .subscribe().with(
                airProcessor::onNext,
                err -> Log.error("Air quality stream error", err));

        battery.filter(Objects::nonNull)
            .subscribe().with(
                batteryProcessor::onNext,
                err -> Log.error("Battery stream error", err));

        smoke.filter(Objects::nonNull)
            .subscribe().with(
                smokeProcessor::onNext,
                err -> Log.error("Smoke stream error", err));

        alert.filter(Objects::nonNull)
            .subscribe().with(
                alertProcessor::onNext,
                err -> Log.error("Alert stream error", err));
    }

    public Multi<PowerConsumptionPoint> streamPower(long deviceId) {
        return powerProcessor.filter(e -> e.deviceId() == deviceId);
    }

    public Multi<TemperaturePoint> streamTemperature(long deviceId) {
        return temperatureProcessor.filter(e -> e.deviceId() == deviceId);
    }

    public Multi<AirQualityPoint> streamAir(long deviceId) {
        return airProcessor.filter(e -> e.deviceId() == deviceId);
    }

    public Multi<BatteryPoint> streamBattery(long deviceId) {
        return batteryProcessor.filter(e -> e.deviceId() == deviceId);
    }

    public Multi<SmokeDetectorPoint> streamSmoke(long deviceId) {
        return smokeProcessor.filter(e -> e.deviceId() == deviceId);
    }

    public Multi<Alert> streamAlert(long deviceId) {
        return alertProcessor.filter(e -> e.dID() == deviceId);
    }
}