package dev.nez;

import dev.nez.dto.LoginRequest;
import dev.nez.dto.LoginResponse;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Duration;
import java.util.List;

@Path("/api/device")
public class AuthResource {

    @ConfigProperty(name = "auth.jwt.issuer", defaultValue = "auth-service")
    String jwtIssuer;

    @ConfigProperty(name = "jwt.key.id")
    String keyId;

    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<LoginResponse>> login(LoginRequest request) {
        Context context = Vertx.currentContext();

        return Device
                .findByHardwareId(request.hardwareId())
                .onItem().transformToUni(device -> {
                    if (device == null || device.status != Device.Status.ACTIVE) {
                        return Uni.createFrom().item(
                                RestResponse.status(RestResponse.Status.UNAUTHORIZED));
                    }

                    return Uni.createFrom()
                            .item(() -> BcryptUtil.matches(request.password(), device.passwordHash))
                            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                            .emitOn(command -> context.runOnContext(_ -> command.run()))
                            .onItem().transform(isMatch -> {
                                if (!isMatch) {
                                    return RestResponse.<LoginResponse>status(RestResponse.Status.UNAUTHORIZED);
                                }

                                String token = Jwt.issuer(jwtIssuer)
                                        .subject(String.valueOf(device.id))
                                        .expiresIn(Duration.ofHours(6))
                                        .claim("publ", List.of(device.topic))
                                        .jws().keyId(keyId)
                                        .sign();

                                return RestResponse.ok(new LoginResponse(device.id, token));
                            })
                            .onFailure().recoverWithItem(throwable -> {
                                Log.error("Internal server error during token generation:", throwable);
                                return RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR);
                            });
                });
    }

    @POST
    @Path("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<RegisterResponse>> register(RegisterRequest request) {
        Context context = Vertx.currentContext();

        return Uni.createFrom()
                .item(() -> BcryptUtil.bcryptHash(request.password(), 8))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .emitOn(command -> context.runOnContext(_ -> command.run()))
                .chain(hashedPassword -> Panache.<Device>withTransaction(() -> {
                        Device device = new Device();
                        device.hardwareId = request.hardwareId();
                        device.passwordHash = hashedPassword;
                        device.topic = request.topic();
                        device.status = Device.Status.ACTIVE;

                        return device.persist();
                }))
                .onItem().transform(persistedDevice -> RestResponse.ok(
                        new RegisterResponse(
                            persistedDevice.id,
                            persistedDevice.hardwareId,
                            request.password(), //TODO remove pass
                            persistedDevice.topic
                )))
                .onFailure(PersistenceException.class).recoverWithItem(
                    () -> RestResponse.status(RestResponse.Status.CONFLICT));
    }
}
