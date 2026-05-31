package dev.nez.configuration.resource;
import dev.nez.configuration.client.ProducerConfigClient;
import dev.nez.configuration.dto.conf.ProducerConfig;

import io.quarkus.logging.Log;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;

import jakarta.inject.Inject;
import jakarta.validation.Valid;

import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.temporal.ChronoUnit;

@Path("/api/producer")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
public class ProducerConfigResource {

    @Inject
    @RestClient
    ProducerConfigClient producerConfigClient;

    @GET
    @Path("/gen")
    @RateLimit(value = 1, window = 500, windowUnit = ChronoUnit.MILLIS)
    public Uni<RestResponse<ProducerConfig>> getConfig() {
        return producerConfigClient.getConfig()
            .map(RestResponse::getEntity)
            .map(RestResponse::ok);
    }

    @POST
    @Path("/gen/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @RateLimit(value = 1, window = 500, windowUnit = ChronoUnit.MILLIS)
    public Uni<RestResponse<ProducerConfig>> updateGenerate(@Valid ProducerConfig request) {
        return producerConfigClient.updateConfig(request)
            .map(RestResponse::getEntity)
            .map(RestResponse::ok);
    }
}