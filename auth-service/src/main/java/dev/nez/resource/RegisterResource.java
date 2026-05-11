package dev.nez.resource;

import dev.nez.dto.PowerThresholdsRequest;
import dev.nez.dto.RegisterRequest;
import dev.nez.model.Device;
import dev.nez.model.PowerThresholds;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/device/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource {

    @ConfigProperty(name = "bcrypt.cost", defaultValue = "8")
    Integer BCRYPT_COST;

    @Inject
    @Channel("thresholds-out")
    MutinyEmitter<PowerThresholdsRequest> thresholdEmitter;

    @POST
    @Path("")
    @RolesAllowed("admin")
    @Bulkhead(value = 100, waitingTaskQueue = 1000)
    public Uni<RestResponse<Void>> register(@Valid RegisterRequest request) {
        return Device.findByHardwareId(request.hardwareId()).chain(result -> {
            if (result != null) {
                return Uni.createFrom().item(RestResponse.status(RestResponse.Status.CONFLICT));
            }

            return Uni.createFrom()
                .item(() -> BcryptUtil.bcryptHash(request.password(), BCRYPT_COST))
                .chain(hashedPassword -> Panache.<Device>withTransaction(() -> {
                    final var device = new Device(
                        request.hardwareId(),
                        hashedPassword,
                        Device.Status.ACTIVE,
                        request.messageType(),
                        request.topic(),
                        request.batteryTopic()
                    );

                    return device.persist();
                }))
                .replaceWith(RestResponse.<Void>status(RestResponse.Status.CREATED))
                .onFailure(PersistenceException.class).recoverWithItem(ex -> {
                    Log.warn("Error: ", ex);
                    return RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR);
                })
                .onFailure(ConstraintViolationException.class)
                .recoverWithItem(RestResponse.status(RestResponse.Status.CONFLICT));
        });
    }

    @POST
    @Path("/thresholds/power")
    @RolesAllowed("admin")
    public Uni<RestResponse<Void>> setThresholds(@Valid PowerThresholdsRequest request) {
        return Device.<Device>findById(request.deviceId())
            .chain(device -> {
                if (device == null || device.status != Device.Status.ACTIVE) {
                    return Uni.createFrom().item(RestResponse.status(RestResponse.Status.NOT_FOUND));
                }

                return Panache.withTransaction(() ->
                   PowerThresholds.<PowerThresholds>findById(request.deviceId())
                       .chain(existingThresholds -> {
                           Uni<Boolean> uni;

                           if (existingThresholds != null) {
                               existingThresholds.minVoltage = request.minVoltage();
                               existingThresholds.maxVoltage = request.maxVoltage();
                               existingThresholds.maxCurrent = request.maxCurrent();
                               existingThresholds.maxPower = request.maxPower();
                               uni = Uni.createFrom().item(false);
                           } else {
                               final var newThresholds = new PowerThresholds(
                                   request.deviceId(),
                                   request.minVoltage(),
                                   request.maxVoltage(),
                                   request.maxCurrent(),
                                   request.maxPower()
                               );
                               uni = newThresholds.persist().replaceWith(true);
                           }

                           return uni.call(() -> thresholdEmitter.sendMessage(
                               Message.of(request)
                                   .withMetadata(Metadata.of(
                                       OutgoingKafkaRecordMetadata.<Long>builder().withKey(request.deviceId()).build()
                                   ))
                           ));
                       })
                    )
                    .map(isCreated -> isCreated
                        ? RestResponse.<Void>status(RestResponse.Status.CREATED)
                        : RestResponse.<Void>status(RestResponse.Status.OK)
                    )
                    .onFailure(PersistenceException.class).recoverWithItem(ex -> {
                        Log.warn("Error saving thresholds: ", ex);
                        return RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR);
                    })
                    .onFailure(ConstraintViolationException.class)
                    .recoverWithItem(RestResponse.status(RestResponse.Status.CONFLICT));
            });
    }
}