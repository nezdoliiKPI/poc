package dev.nez.panel;

import dev.nez.panel.dto.EdgeConfigUpdate;
import dev.nez.panel.dto.ProducerConfigUpdate;

import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.Simulator;

import io.smallrye.common.annotation.RunOnVirtualThread;

import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.vertx.core.eventbus.EventBus;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import jakarta.validation.Valid;

import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Path("/api/panel/update")
public class ConfigResource {

    @Inject
    EventBus eventBus;

    @Inject
    SimulationConfig config;

    @Inject
    @RestClient
    EdgeConfigClient  edgeConfigClient;

    @GET
    @Path("/page")
    @Produces(MediaType.TEXT_HTML)
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public InputStream getPanelPage() {
        return getClass().getResourceAsStream("/META-INF/resources/index.html");
    }

    @GET
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<Void> verifyAuth() {
        return RestResponse.ok();
    }

    @POST
    @Path("/gen")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @RunOnVirtualThread
    @RateLimit(value = 1, window = 500, windowUnit = ChronoUnit.MILLIS)
    public RestResponse<Float> updateGenerate(@Valid ProducerConfigUpdate request) {
        var events = List.of(
            new Simulator.ConfigChangeEvent(config.air().json().topic(), request.airJsonCount()),
            new Simulator.ConfigChangeEvent(config.air().proto().topic(), request.airProtoCount()),
            new Simulator.ConfigChangeEvent(config.power().json().topic(), request.powerJsonCount()),
            new Simulator.ConfigChangeEvent(config.power().proto().topic(), request.powerProtoCount()),
            new Simulator.ConfigChangeEvent(config.smoke().json().topic(), request.smokeJsonCount()),
            new Simulator.ConfigChangeEvent(config.smoke().proto().topic(), request.smokeProtoCount())
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
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<Void> updateEdge(EdgeConfigUpdate request) {
        return edgeConfigClient.updateConfig(request);
    }

    @ServerExceptionMapper
    public RestResponse<String> mapBulkheadException(BulkheadException ex) {
        return RestResponse.status(
            RestResponse.Status.TOO_MANY_REQUESTS,
            "Your request is currently being processed. Please try again later."
        );
    }

    @ServerExceptionMapper
    public RestResponse<String> handleRateLimit(RateLimitException ex) {
        return RestResponse.status(
            RestResponse.Status.TOO_MANY_REQUESTS,
            "Too Many Requests: Please slow down."
        );
    }
}