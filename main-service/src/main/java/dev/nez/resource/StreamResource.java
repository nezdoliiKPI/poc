package dev.nez.resource;

import dev.nez.consumer.TelemetryConsumer;
import dev.nez.model.*;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/devices/{id}/stream")
@RolesAllowed("admin")
public class StreamResource {

    @Inject
    TelemetryConsumer consumer;

    @GET
    @Path("/power")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<PowerConsumptionPoint> power(
        @PathParam("id") long deviceId
    ) {
        return consumer.streamPower(deviceId);
    }

    @GET
    @Path("/temperature")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<TemperaturePoint> temperature(
        @PathParam("id") long deviceId
    ) {
        return consumer.streamTemperature(deviceId);
    }

    @GET
    @Path("/air-quality")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<AirQualityPoint> airQuality(
        @PathParam("id") long deviceId
    ) {
        return consumer.streamAir(deviceId);
    }

    @GET
    @Path("/battery")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<BatteryPoint> battery(
        @PathParam("id") long deviceId
    ) {
        return consumer.streamBattery(deviceId);
    }

    @GET
    @Path("/smoke")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<SmokeDetectorPoint> smoke(
        @PathParam("id") long deviceId
    ) {
        return consumer.streamSmoke(deviceId);
    }
}