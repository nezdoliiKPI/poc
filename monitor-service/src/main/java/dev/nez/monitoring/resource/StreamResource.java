package dev.nez.monitoring.resource;

import dev.nez.monitoring.consumer.KafkaConsumer;
import dev.nez.monitoring.dto.*;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;

@Path("/api/devices/{id}/stream")
@RolesAllowed("admin")
@Produces(MediaType.SERVER_SENT_EVENTS)
@Valid
public class StreamResource {

    @Inject
    KafkaConsumer consumer;

    @GET
    @Path("/power")
    public Multi<PowerConsumptionPoint> power(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamPower(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }

    @GET
    @Path("/temperature")
    public Multi<TemperaturePoint> temperature(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamTemperature(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }

    @GET
    @Path("/air-quality")
    public Multi<AirQualityPoint> airQuality(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamAir(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }

    @GET
    @Path("/battery")
    public Multi<BatteryPoint> battery(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamBattery(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }

    @GET
    @Path("/smoke")
    public Multi<SmokeDetectorPoint> smoke(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamSmoke(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }

    @GET
    @Path("/alert")
    public Multi<Alert> alert(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamAlert(deviceId)
            .filter(point -> point.ts().isAfter(since))
            .onOverflow().bufferUnconditionally();
    }
}