package dev.nez.monitoring.resource;

import dev.nez.monitoring.dto.*;
import dev.nez.monitoring.service.HistoryService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

@Path("/api/devices/{id}/history")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Valid
public class HistoryResource {

    @Inject
    HistoryService historyService;

    @GET
    @Path("/power")
    public Uni<List<PowerConsumptionPoint>> power(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getPowerHistory(deviceId, from, to);
    }

    @GET
    @Path("/temperature")
    public Uni<List<TemperaturePoint>> temperature(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getTemperatureHistory(deviceId, from, to);
    }

    @GET
    @Path("/air-quality")
    public Uni<List<AirQualityPoint>> airQuality(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getAirQualityHistory(deviceId, from, to);
    }

    @GET
    @Path("/battery")
    public Uni<List<BatteryPoint>> battery(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getBatteryHistory(deviceId, from, to);
    }

    @GET
    @Path("/smoke")
    public Uni<List<SmokeDetectorPoint>> smoke(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getSmokeHistory(deviceId, from, to);
    }

    @GET
    @Path("/alert")
    public Uni<List<Alert>> alert(
        @PathParam("id")    @NotNull Long deviceId,
        @QueryParam("from") @NotNull @PastOrPresent OffsetDateTime from,
        @QueryParam("to")   @NotNull @PastOrPresent OffsetDateTime to
    ) {
        return historyService.getAlertHistory(deviceId, from, to);
    }
}