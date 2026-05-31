package dev.nez.configuration.client;

import dev.nez.configuration.dto.conf.ProducerConfig;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "producer-api")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public interface ProducerConfigClient {

    @GET
    @Path("/gen")
    Uni<RestResponse<ProducerConfig>> getConfig();

    @POST
    @Path("/gen/update")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<RestResponse<ProducerConfig>> updateConfig(ProducerConfig request);
}
