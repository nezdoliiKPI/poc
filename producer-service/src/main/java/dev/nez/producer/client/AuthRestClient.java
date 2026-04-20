package dev.nez.producer.client;

import dev.nez.producer.dto.rest.LoginRequest;
import dev.nez.producer.dto.rest.LoginResponse;
import dev.nez.producer.dto.rest.RegisterRequest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

@RegisterRestClient(configKey = "auth-api")
public interface AuthRestClient {

    @POST
    @Path("/api/device/auth/register")
    @Retry(maxRetries = 5, delay = 3, delayUnit = ChronoUnit.SECONDS)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> register(RegisterRequest request);

    @POST
    @Path("/api/device/auth/login")
    @Retry(maxRetries = 5, delay = 3, delayUnit = ChronoUnit.SECONDS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<LoginResponse> login(LoginRequest request);
}
