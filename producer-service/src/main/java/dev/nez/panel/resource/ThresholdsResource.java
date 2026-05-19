package dev.nez.panel.resource;

import dev.nez.panel.dto.kafka.*;
import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.Simulations;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.annotation.security.RolesAllowed;

@Path("/api/panel/thresholds")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
public class ThresholdsResource {

    @Inject
    SimulationConfig config;

    @Inject
    Simulations simulations;

    @Inject
    @Channel("air-thresholds-out")
    MutinyEmitter<AirQualityThresholds> airEmitter;

    @Inject
    @Channel("battery-thresholds-out")
    MutinyEmitter<BatteryThresholds> batteryEmitter;

    @Inject
    @Channel("power-thresholds-out")
    MutinyEmitter<PowerThresholds> powerEmitter;

    @Inject
    @Channel("smoke-thresholds-out")
    MutinyEmitter<SmokeDetectorThresholds> smokeEmitter;

    @Inject
    @Channel("temperature-thresholds-out")
    MutinyEmitter<TemperatureThresholds> tempEmitter;

    @POST
    @Path("/air")
    @RunOnVirtualThread
    public RestResponse<Void> updateAirThresholds(@Valid AirQualityThresholds thresholds) {
        final var emitter = airEmitter;

        if (thresholds.deviceId() != null) {
            sendMessage(thresholds, thresholds.deviceId(), emitter);
        } else {
            sendMessages(thresholds, config.air().proto().topic(), emitter);
            sendMessages(thresholds, config.air().json().topic(), emitter);
        }

        return RestResponse.ok();
    }

    @POST
    @Path("/battery")
    @RunOnVirtualThread
    public RestResponse<Void> updateBatteryThresholds(@Valid BatteryThresholds thresholds) {
        final var emitter = batteryEmitter;

        if (thresholds.deviceId() != null) {
            sendMessage(thresholds, thresholds.deviceId(), emitter);
        } else {
            sendMessages(thresholds, config.battery().proto().topic(), emitter);
            sendMessages(thresholds, config.battery().json().topic(), emitter);
        }

        return RestResponse.ok();
    }

    @POST
    @Path("/power")
    @RunOnVirtualThread
    public RestResponse<Void> updatePowerThresholds(@Valid PowerThresholds thresholds) {
        final var emitter  = powerEmitter;

        if (thresholds.deviceId() != null) {
            sendMessage(thresholds, thresholds.deviceId(), emitter);
        } else {
            sendMessages(thresholds, config.power().proto().topic(), emitter);
            sendMessages(thresholds, config.power().json().topic(), emitter);
        }

        return RestResponse.ok();
    }

    @POST
    @Path("/smoke")
    @RunOnVirtualThread
    public RestResponse<Void> updateSmokeThresholds(@Valid SmokeDetectorThresholds thresholds) {
        final var emitter = smokeEmitter;

        if (thresholds.deviceId() != null) {
            sendMessage(thresholds, thresholds.deviceId(), emitter);
        } else {
            sendMessages(thresholds, config.smoke().proto().topic(), emitter);
            sendMessages(thresholds, config.smoke().json().topic(), emitter);
        }

        return RestResponse.ok();
    }

    @POST
    @Path("/temperature")
    @RunOnVirtualThread
    public RestResponse<Void> updateTemperatureThresholds(@Valid TemperatureThresholds thresholds) {
        final var emitter  = tempEmitter;

        if (thresholds.deviceId() != null) {
            sendMessage(thresholds, thresholds.deviceId(), emitter);
        } else {
            sendMessages(thresholds, config.temp().proto().topic(), emitter);
            sendMessages(thresholds, config.temp().json().topic(), emitter);
        }

        return RestResponse.ok();
    }

    private <T>  void sendMessage(T thresholds, Long deviceId, MutinyEmitter<T> emitter) {
        var kafkaMetadata = OutgoingKafkaRecordMetadata.<Long>builder()
            .withKey(deviceId)
            .build();

        emitter.sendMessageAndAwait(Message.of(thresholds).addMetadata(kafkaMetadata));
    }

    private <T>  void sendMessages(T thresholds, String topic, MutinyEmitter<T> emitter) {
        final var ids = simulations.getSessionIds(topic);

        for (var deviceId : ids) {
            sendMessage(thresholds, deviceId, emitter);
        }
    }
}
