package dev.nez.monitoring.resource;

import dev.nez.monitoring.consumer.TelemetryConsumer;
import dev.nez.monitoring.model.*;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;

@Path("/api/devices/{id}/stream")
@RolesAllowed("admin")
@Produces(MediaType.SERVER_SENT_EVENTS)
public class StreamResource {

    @Inject
    TelemetryConsumer consumer;

    @GET
    @Path("/power")
    public Multi<PowerConsumptionPoint> power(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamPower(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().dropPreviousItems();
    }

    @GET
    @Path("/temperature")
    public Multi<TemperaturePoint> temperature(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamTemperature(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().dropPreviousItems();
    }

    @GET
    @Path("/air-quality")
    public Multi<AirQualityPoint> airQuality(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamAir(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().dropPreviousItems();
    }

    @GET
    @Path("/battery")
    public Multi<BatteryPoint> battery(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamBattery(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().dropPreviousItems();
    }

    @GET
    @Path("/smoke")
    public Multi<SmokeDetectorPoint> smoke(
        @PathParam("id") long deviceId,
        @QueryParam("since") Instant since
    ) {
        return consumer.streamSmoke(deviceId)
            .filter(point -> point.timeDate().isAfter(since))
            .onOverflow().dropPreviousItems();
    }
}