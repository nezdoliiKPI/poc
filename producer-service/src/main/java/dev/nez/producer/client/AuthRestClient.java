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
@Path("/api/device/auth")
public interface AuthRestClient {

    @POST
    @Path("/register")
    @Retry(maxRetries = 15, delay = 1, delayUnit = ChronoUnit.SECONDS, jitter = 500, jitterDelayUnit = ChronoUnit.MILLIS)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> register(RegisterRequest request);

    @POST
    @Path("/login")
    @Retry(maxRetries = 15, delay = 1, delayUnit = ChronoUnit.SECONDS, jitter = 500, jitterDelayUnit = ChronoUnit.MILLIS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<LoginResponse> login(LoginRequest request);
}
