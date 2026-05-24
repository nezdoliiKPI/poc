package dev.nez.service;

import dev.nez.dto.RegisterRequest;
import dev.nez.model.Device;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DeviceService {

    @CacheName("device-cache")
    Cache cache;

    @ConfigProperty(name = "bcrypt.cost", defaultValue = "8")
    Integer BCRYPT_COST;

    public Uni<Device> registerDevice(RegisterRequest request) {
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

            return device.<Device>persist();
        })
        .call(savedDevice -> cache.invalidate(request.hardwareId())
            .chain(() -> cache.get(
               request.hardwareId(),
               _ -> savedDevice
           ))
        );
    }

    public Uni<Device> findDeviceByHardwareId(String hardwareId) {
        return cache.getAsync(hardwareId, Device::findByHardwareId);
    }
}
