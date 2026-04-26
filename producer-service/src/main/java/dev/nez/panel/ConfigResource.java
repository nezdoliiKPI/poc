package dev.nez.panel;

import dev.nez.panel.dto.EdgeConfigUpdate;
import dev.nez.panel.dto.ProducerConfigUpdate;

import dev.nez.producer.simulation.SimulationConfig;
import dev.nez.producer.simulation.Simulator;

import io.smallrye.common.annotation.RunOnVirtualThread;

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
    @Bulkhead(value = 1)
    public RestResponse<Float> updateGenerate(@Valid ProducerConfigUpdate request) {
        eventBus.send(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.air().json().topic(), request.airJsonCount())
        );
        eventBus.send(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.air().proto().topic(), request.airProtoCount())
        );
        eventBus.send(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.power().json().topic(), request.powerJsonCount())
        );
        eventBus.send(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.power().proto().topic(), request.powerProtoCount())
        );
        eventBus.send(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.smoke().json().topic(), request.smokeJsonCount())
        );
        var result = eventBus.request(Simulator.CONFIG_ADDRESS,
            new Simulator.ConfigChangeEvent(config.smoke().proto().topic(), request.smokeProtoCount())
        )
            .toCompletionStage()
            .toCompletableFuture()
            .join();

        return RestResponse.ok((Float) result.body());
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
}