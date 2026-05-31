package dev.nez.producer.resource;

import dev.nez.producer.dto.rest.ProducerConfig;
import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.Simulator;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/api")
public class ConfigResource {

    @Inject
    Simulator simulator;

    @Inject
    SimulationConfig config;

    @GET
    @Path("/gen")
    public Uni<RestResponse<ProducerConfig>> getConfig() {
        return simulator.getConfig()
            .map(RestResponse::ok);
    }

    @POST
    @Path("/gen/update")
    @RateLimit(value = 1, window = 500, windowUnit = ChronoUnit.MILLIS)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ProducerConfig>> updateGenerate(@Valid ProducerConfig request) {
        final var events = List.of(
            new Simulator.ConfigChangeEvent(config.air().json().topic(), request.airJsonCount()),
            new Simulator.ConfigChangeEvent(config.air().proto().topic(), request.airProtoCount()),
            new Simulator.ConfigChangeEvent(config.power().json().topic(), request.powerJsonCount()),
            new Simulator.ConfigChangeEvent(config.power().proto().topic(), request.powerProtoCount()),
            new Simulator.ConfigChangeEvent(config.smoke().json().topic(), request.smokeJsonCount()),
            new Simulator.ConfigChangeEvent(config.smoke().proto().topic(), request.smokeProtoCount()),
            new Simulator.ConfigChangeEvent(config.temp().json().topic(), request.tempJsonCount()),
            new Simulator.ConfigChangeEvent(config.temp().proto().topic(), request.tempProtoCount())
        );

        final var unis = events.stream()
            .map(event -> simulator.configChange(event))
            .toList();

        return Uni.combine().all().unis(unis)
            .discardItems()
            .chain(() -> simulator.getConfig())
            .map(RestResponse::ok);
    }
}