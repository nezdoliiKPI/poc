package dev.nez.panel.resource;

import dev.nez.panel.dto.ConfigUpdate;
import dev.nez.producer.simulation.Simulator;
import dev.nez.producer.simulation.config.DynamicConfig;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/panel")
public class ConfigResource {

    @Inject
    DynamicConfig config;

    @Inject
    Simulator simulator;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public synchronized RestResponse<Float> update(ConfigUpdate request) {
        config.setAirJsonCount(request.airJsonCount());
        config.setAirProtoCount(request.airProtoCount());
        config.setPowerJsonCount(request.powerJsonCount());
        config.setPowerProtoCount(request.powerProtoCount());
        config.setSmokeProtoCount(request.smokeProtoCount());
        config.setSmokeJsonCount(request.smokeJsonCount());

        return RestResponse.ok(simulator.getIntensity());
    }
}