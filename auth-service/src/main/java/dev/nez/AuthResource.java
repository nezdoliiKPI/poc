package dev.nez;

import dev.nez.dto.LoginRequest;
import dev.nez.dto.RegisterRequest;
import dev.nez.dto.RegisterResponse;
import io.quarkus.elytron.security.common.BcryptUtil;

import io.vertx.core.Vertx;
import io.vertx.core.Context;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.logging.Log;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Path("/api/device")
public class AuthResource {

    @ConfigProperty(name = "auth.jwt.issuer", defaultValue = "auth-service")
    String jwtIssuer;

    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> login(LoginRequest request) {
        Context context = Vertx.currentContext();

        return Device.<Device>findByHardwareId(request.hardwareId())
                .onItem().transformToUni(device -> {
                    if (device == null || device.status != Device.Status.ACTIVE) {
                        return Uni.createFrom().item(
                            Response.status(Response.Status.UNAUTHORIZED)
                                    .entity("Device not found or inactive")
                                    .build()
                        );
                    }

                    return Uni.createFrom()
                            .item(() -> BcryptUtil.matches(request.password(), device.passwordHash))
                            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                            .emitOn(command -> context.runOnContext(v -> command.run()))
                            .onItem().transform(isMatch -> {
                                if (!isMatch) {
                                    return Response.status(Response.Status.UNAUTHORIZED)
                                            .entity("Invalid credentials")
                                            .build();
                                }

                                String token = Jwt.issuer(jwtIssuer)
                                        .subject(request.hardwareId())
                                        .expiresIn(Duration.ofHours(12))
                                        .claim("pub", List.of(device.topic))
                                        .sign();

                                return Response
                                        .ok(Map.of("token", token))
                                        .build();
                            })
                            .onFailure().recoverWithItem(throwable -> {
                                Log.error("Internal server error during token generation");
                                return Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(Map.of("error", "Internal server error during token generation"))
                                        .build();
                            });
                });
    }

    @POST
    @Path("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> register(RegisterRequest request) {
        Context context = Vertx.currentContext();

        return Uni.createFrom()
                .item(() -> BcryptUtil.bcryptHash(request.password()))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .emitOn(command -> context.runOnContext(v -> command.run()))
                .chain(hashedPassword -> Panache.<Device>withTransaction(() -> {
                        Device device = new Device();
                        device.hardwareId = request.hardwareId();
                        device.passwordHash = hashedPassword;
                        device.topic = request.topic();
                        device.status = Device.Status.ACTIVE;

                        return device.persist();
                }))
                .onItem().transform(persistedDevice -> {
                    return Response.status(Response.Status.CREATED)
                        .entity(new RegisterResponse(
                                persistedDevice.id,
                                persistedDevice.hardwareId,
                                request.password(), //TODO remove pass
                                persistedDevice.topic
                        ))
                        .build();
        })
        .onFailure(PersistenceException.class)
        .recoverWithItem(() -> Response
                                    .status(Response.Status.CONFLICT)
                                    .entity("Device already exists")
                                    .build()
        );
    }
}
