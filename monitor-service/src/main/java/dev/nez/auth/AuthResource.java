package dev.nez.auth;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Valid
public class AuthResource {
    @ConfigProperty(name = "ADMIN_USERNAME") String adminUsername;
    @ConfigProperty(name = "ADMIN_PASSWORD") String adminPassword;

    @Inject
    SessionStore sessionStore;

    @Inject
    SecurityIdentity identity;

    public record LoginRequest(
        @NotNull String username,
        @NotNull String password
    ) {}

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Response login(LoginRequest req, @Context UriInfo uriInfo) {
        if (!adminUsername.equals(req.username()) || !adminPassword.equals(req.password())) {
            return Response.status(401).entity(Map.of("error", "Invalid credentials")).build();
        }

        final String token = sessionStore.createSession(req.username());
        final boolean secure = uriInfo.getBaseUri().getScheme().equals("https");

        NewCookie cookie = new NewCookie.Builder(CookieAuthMechanism.COOKIE_NAME)
            .value(token)
            .path("/")
            .httpOnly(true)
            .secure(secure)
            .sameSite(NewCookie.SameSite.STRICT)
            .maxAge(30 * 60)
            .build();

        return Response.ok().cookie(cookie).build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed("admin")
    @RunOnVirtualThread
    public Response logout(@CookieParam(CookieAuthMechanism.COOKIE_NAME) String token) {
        sessionStore.remove(token);

        final NewCookie clear = new NewCookie.Builder(CookieAuthMechanism.COOKIE_NAME)
            .value("")
            .path("/")
            .httpOnly(true)
            .maxAge(0)
            .build();

        return Response.ok().cookie(clear).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed("admin")
    @RunOnVirtualThread
    public Response me() {
        return Response.ok(Map.of("username", identity.getPrincipal().getName())).build();
    }
}