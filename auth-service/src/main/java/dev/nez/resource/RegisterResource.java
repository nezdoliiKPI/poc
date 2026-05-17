package dev.nez.resource;

import dev.nez.dto.RegisterRequest;
import dev.nez.model.Device;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/device/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource {

    @ConfigProperty(name = "bcrypt.cost", defaultValue = "8")
    Integer BCRYPT_COST;

    @POST
    @Path("")
    @RolesAllowed("admin")
    @Bulkhead(value = 100, waitingTaskQueue = 1000)
    public Uni<RestResponse<Void>> register(@Valid RegisterRequest request) {
        return Device.findByHardwareId(request.hardwareId()).chain(result -> {
            if (result != null) {
                return Uni.createFrom().item(RestResponse.status(RestResponse.Status.CONFLICT));
            }

            return Panache.withTransaction(() -> {
                    final var hashedPass = BcryptUtil.bcryptHash(request.password(), BCRYPT_COST);

                    final var device = new Device(
                        request.hardwareId(),
                        hashedPass,
                        Device.Status.ACTIVE,
                        request.messageType(),
                        request.topic(),
                        request.batteryTopic()
                    );

                    return device.persist();
                })
                .replaceWith(RestResponse.<Void>status(RestResponse.Status.CREATED))
                .onFailure(PersistenceException.class).recoverWithItem(ex -> {
                    Log.warn("Error: ", ex);
                    return RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR);
                })
                .onFailure(ConstraintViolationException.class)
                .recoverWithItem(RestResponse.status(RestResponse.Status.CONFLICT));
        });
    }
}