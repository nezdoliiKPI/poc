package dev.nez.edge.service.messaging;

import dev.nez.edge.data.PowerConsumptionData;
import dev.nez.edge.dto.MessageMapper;
import dev.nez.edge.interceptor.RecordConsumingMessage;

import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class PowerConsumptionService {
    private static final String CHANNEL_POWER_PROTO_IN = "power-p-in";
    private static final String CHANNEL_POWER_OUT = "power-out";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_POWER_PROTO_IN)
    @Outgoing(CHANNEL_POWER_OUT)
    @RecordConsumingMessage(CHANNEL_POWER_PROTO_IN)
    public Uni<PowerConsumptionData> consumePowerProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromProtoPowerConsumption(p));
    }
}
