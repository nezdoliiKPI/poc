package dev.nez.resource;

import dev.nez.dto.LoginRequest;
import dev.nez.dto.LoginResponse;

import dev.nez.model.Device;
import io.quarkus.elytron.security.common.BcryptUtil;

import io.quarkus.logging.Log;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

import org.jboss.resteasy.reactive.RestResponse;

import java.time.Duration;
import java.util.List;

@Path("/api/device/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @ConfigProperty(name = "auth.jwt.issuer", defaultValue = "auth-service")
    String jwtIssuer;

    @ConfigProperty(name = "jwt.key.id")
    String keyId;

    @POST
    @Path("/login")
    @Bulkhead(value = 100, waitingTaskQueue = 1000)
    public Uni<RestResponse<LoginResponse>> login(@Valid LoginRequest request) {
        return Device.findByHardwareId(request.hardwareId())
                .chain(device -> {
                    if (device == null || device.status != Device.Status.ACTIVE) {
                        return Uni.createFrom().item(RestResponse.status(RestResponse.Status.UNAUTHORIZED));
                    }

                    return Uni.createFrom()
                            .item(() -> BcryptUtil.matches(request.password(), device.passwordHash))
                            .map(isMatch -> {
                                if (!isMatch) {
                                    return RestResponse.<LoginResponse>status(RestResponse.Status.UNAUTHORIZED);
                                }

                                final var topics = device.getBatteryTopic().isPresent()
                                        ? List.of(device.topic, device.batteryTopic)
                                        : List.of(device.topic);

                                final String token = Jwt.issuer(jwtIssuer)
                                        .subject(String.valueOf(device.id))
                                        .expiresIn(Duration.ofHours(6))
                                        .claim("publ", topics)
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
}