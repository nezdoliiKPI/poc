package dev.nez.consumer.service;

import dev.nez.consumer.interceptor.RecordConsumingMessage;
import dev.nez.proto.timeddata.PowerConsumptionData;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class PowerConsumptionService {
    private static final String CHANNEL_POWER_IN = "power-in";

    @Incoming(CHANNEL_POWER_IN)
    @RecordConsumingMessage(CHANNEL_POWER_IN)
    public Uni<PowerConsumptionData> consumePowerProto(PowerConsumptionData data) {
        return Uni.createFrom().item(data);
    }
}
