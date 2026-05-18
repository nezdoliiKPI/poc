package dev.nez.panel.resource;

import dev.nez.panel.dto.kafka.*;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.annotation.security.RolesAllowed;

@Path("/api/panel/thresholds")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
public class ThresholdsResource {

    @Inject
    @Channel("air-thresholds-out")
    Emitter<AirQualityThresholds> airEmitter;

    @Inject
    @Channel("battery-thresholds-out")
    Emitter<BatteryThresholds> batteryEmitter;

    @Inject
    @Channel("power-thresholds-out")
    Emitter<PowerThresholds> powerEmitter;

    @Inject
    @Channel("smoke-thresholds-out")
    Emitter<SmokeDetectorThresholds> smokeEmitter;

    @Inject
    @Channel("temperature-thresholds-out")
    Emitter<TemperatureThresholds> tempEmitter;

    @POST
    @Path("/air")
    @RunOnVirtualThread
    public RestResponse<Void> updateAirThresholds(@Valid AirQualityThresholds thresholds) {
        final var emitter = airEmitter;
        sendMessage(thresholds, thresholds.deviceId(), emitter);
        return RestResponse.ok();
    }

    @POST
    @Path("/battery")
    @RunOnVirtualThread
    public RestResponse<Void> updateBatteryThresholds(@Valid BatteryThresholds thresholds) {
        final var emitter = batteryEmitter;
        sendMessage(thresholds, thresholds.deviceId(), emitter);
        return RestResponse.ok();
    }

    @POST
    @Path("/power")
    @RunOnVirtualThread
    public RestResponse<Void> updatePowerThresholds(@Valid PowerThresholds thresholds) {
        final var emitter  = powerEmitter;
        sendMessage(thresholds, thresholds.deviceId(), emitter);
        return RestResponse.ok();
    }

    @POST
    @Path("/smoke")
    @RunOnVirtualThread
    public RestResponse<Void> updateSmokeThresholds(@Valid SmokeDetectorThresholds thresholds) {
        final var emitter = smokeEmitter;
        sendMessage(thresholds, thresholds.deviceId(), emitter);
        return RestResponse.ok();
    }

    @POST
    @Path("/temperature")
    @RunOnVirtualThread
    public RestResponse<Void> updateTemperatureThresholds(@Valid TemperatureThresholds thresholds) {
        final var emitter  = tempEmitter;
        sendMessage(thresholds, thresholds.deviceId(), emitter);
        return RestResponse.ok();
    }

    private <T>  void sendMessage(T thresholds, Long deviceId, Emitter<T> emitter) {
        var kafkaMetadata = OutgoingKafkaRecordMetadata.<Long>builder()
            .withKey(deviceId)
            .build();

        emitter.send(Message.of(thresholds).addMetadata(kafkaMetadata));
    }
}
