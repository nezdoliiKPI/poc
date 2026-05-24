package dev.nez.monitoring.resource;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/auth")
public class AuthResource {

    @GET
    @Path("/verify")
    @RolesAllowed("admin")
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<Void> verifyAuth() {
        return RestResponse.ok();
    }
}
