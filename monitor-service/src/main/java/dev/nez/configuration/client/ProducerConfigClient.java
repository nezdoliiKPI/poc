package dev.nez.configuration.client;

import dev.nez.configuration.dto.conf.ProducerConfig;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "producer-api")
@Path("/api/producer/update")
public interface ProducerConfigClient {

    @POST
    @Path("/gen")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<RestResponse<Float>> updateConfig(ProducerConfig request);
}
