package dev.nez.simulation.client;

import dev.nez.simulation.dto.LoginRequest;
import dev.nez.simulation.dto.LoginResponse;
import dev.nez.simulation.dto.RegisterRequest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "auth-api")
public interface AuthRestClient {

    @POST
    @Path("/api/device/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> register(RegisterRequest request);

    @POST
    @Path("/api/device/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<LoginResponse> login(LoginRequest request);
}
