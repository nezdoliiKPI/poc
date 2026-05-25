package dev.nez.configuration.resource;

import dev.nez.configuration.dto.conf.EdgeConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/panel")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
public class EdgeConfigResource {

    @Inject
    @Channel("filter-config-out")
    MutinyEmitter<EdgeConfig> emitter;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Bulkhead(value = 1)
    public Uni<RestResponse<Void>> update(@Valid EdgeConfig request) {
        return emitter.send(request).map(RestResponse::ok);
    }
}