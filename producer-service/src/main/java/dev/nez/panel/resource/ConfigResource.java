package dev.nez.panel.resource;

import dev.nez.panel.EdgeConfigClient;
import dev.nez.panel.dto.conf.EdgeConfigUpdate;
import dev.nez.panel.dto.conf.ProducerConfigUpdate;

import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.Simulator;

import io.smallrye.common.annotation.RunOnVirtualThread;

import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.eventbus.EventBus;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import jakarta.validation.Valid;

import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Bulkhead;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Path("/api/panel/update")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigResource {

    @Inject
    EventBus eventBus;

    @Inject
    SimulationConfig config;

    @Inject
    @RestClient
    EdgeConfigClient edgeConfigClient;

    @GET
    @Path("/verify")
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<Void> verifyAuth() {
        return RestResponse.ok();
    }

    @POST
    @Path("/gen")
    @RunOnVirtualThread
    @RateLimit(value = 1, window = 500, windowUnit = ChronoUnit.MILLIS)
    public RestResponse<Float> updateGenerate(@Valid ProducerConfigUpdate request) {
        var events = List.of(
            new Simulator.ConfigChangeEvent(config.air().json().topic(), request.airJsonCount()),
            new Simulator.ConfigChangeEvent(config.air().proto().topic(), request.airProtoCount()),
            new Simulator.ConfigChangeEvent(config.power().json().topic(), request.powerJsonCount()),
            new Simulator.ConfigChangeEvent(config.power().proto().topic(), request.powerProtoCount()),
            new Simulator.ConfigChangeEvent(config.smoke().json().topic(), request.smokeJsonCount()),
            new Simulator.ConfigChangeEvent(config.smoke().proto().topic(), request.smokeProtoCount()),
            new Simulator.ConfigChangeEvent(config.temp().json().topic(), request.tempJsonCount()),
            new Simulator.ConfigChangeEvent(config.temp().proto().topic(), request.tempProtoCount())
        );

        var futureResults = events.stream()
            .map(event -> eventBus.request(Simulator.CONFIG_ADDRESS, event)
                .toCompletionStage()
                .toCompletableFuture())
            .toList();

        CompletableFuture.allOf(futureResults.toArray(CompletableFuture[]::new)).join();

        return RestResponse.ok((Float) futureResults.getLast().join().body());
    }

    @POST
    @Path("/edge")
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<Void> updateEdge(EdgeConfigUpdate request) {
        return edgeConfigClient.updateConfig(request);
    }
}