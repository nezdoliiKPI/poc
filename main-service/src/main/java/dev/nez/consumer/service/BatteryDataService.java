package dev.nez.consumer.service;

import dev.nez.consumer.DataMapper;
import dev.nez.consumer.interceptor.InterceptConsumingMessage;
import dev.nez.proto.timeddata.BatteryData;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class BatteryDataService {
    private static final String CHANNEL_BATTERY_IN = "batt-in";

    @Inject
    DataMapper dataMapper;

    @Incoming(CHANNEL_BATTERY_IN)
    @InterceptConsumingMessage(CHANNEL_BATTERY_IN)
    public Uni<BatteryData> consumePowerProto(BatteryData data) {
        return Uni.createFrom().item(data)
            .call(air -> Panache.withTransaction(() -> dataMapper.toEntity(air).persist()));
    }
}
