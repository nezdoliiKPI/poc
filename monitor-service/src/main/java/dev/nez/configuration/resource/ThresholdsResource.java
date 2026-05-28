package dev.nez.configuration.resource;

import dev.nez.configuration.dto.thresholds.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/api/thresholds")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
@Valid
public class ThresholdsResource {

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
    MutinyEmitter<TemperatureThresholds> temperatureEmitter;

    @POST
    @Path("/air")
    public Uni<RestResponse<Void>> updateAirThresholds(List<AirQualityThresholds> thresholdsList) {
        return sendAll(thresholdsList, airEmitter, AirQualityThresholds::deviceId);
    }

    @POST
    @Path("/battery")
    public Uni<RestResponse<Void>> updateBatteryThresholds(List<BatteryThresholds> thresholdsList) {
        return sendAll(thresholdsList, batteryEmitter, BatteryThresholds::deviceId);
    }

    @POST
    @Path("/power")
    public Uni<RestResponse<Void>> updatePowerThresholds(List<PowerThresholds> thresholdsList) {
        return sendAll(thresholdsList, powerEmitter, PowerThresholds::deviceId);
    }

    @POST
    @Path("/smoke")
    public Uni<RestResponse<Void>> updateSmokeThresholds(List<SmokeDetectorThresholds> thresholdsList) {
        return sendAll(thresholdsList, smokeEmitter, SmokeDetectorThresholds::deviceId);
    }

    @POST
    @Path("/temperature")
    public Uni<RestResponse<Void>> updateTemperatureThresholds(List<TemperatureThresholds> thresholdsList) {
        return sendAll(thresholdsList, temperatureEmitter, TemperatureThresholds::deviceId);
    }

    private <T> Uni<RestResponse<Void>> sendAll(List<T> items, MutinyEmitter<T> emitter, Function<T, Long> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Uni.createFrom().item(RestResponse.accepted());
        }

        final List<Uni<Void>> emissions = items.stream()
            .map(item -> emitter.sendMessage(newMessage(item, keyExtractor.apply(item))))
            .collect(Collectors.toList());

        return Uni.join().all(emissions).andFailFast()
            .replaceWith(RestResponse.accepted());
    }

    private <T> Message<T> newMessage(T thresholds, Long deviceId) {
        var kafkaMetadata = OutgoingKafkaRecordMetadata.<Long>builder()
            .withKey(deviceId)
            .build();

        return Message.of(thresholds).addMetadata(kafkaMetadata);
    }
}