package dev.nez.panel;

import dev.nez.panel.dto.EdgeConfigUpdate;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@RegisterRestClient(configKey = "edge-api")
@Path("/api/panel")
public interface EdgeConfigClient {

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    RestResponse<Void> updateConfig(EdgeConfigUpdate request);
}
