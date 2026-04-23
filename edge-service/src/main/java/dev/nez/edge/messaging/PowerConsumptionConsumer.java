package dev.nez.edge.messaging;

import dev.nez.proto.timeddata.PowerConsumptionData;
import dev.nez.edge.dto.MessageMapper;
import dev.nez.edge.interceptor.InterceptConsumingMessage;

import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class PowerConsumptionConsumer {
    private static final String CHANNEL_POWER_JSON_IN = "power-j-in";
    private static final String CHANNEL_POWER_PROTO_IN = "power-p-in";
    private static final String CHANNEL_POWER_OUT = "power-out";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_POWER_PROTO_IN)
    @Outgoing(CHANNEL_POWER_OUT)
    @InterceptConsumingMessage(CHANNEL_POWER_PROTO_IN)
    public Uni<PowerConsumptionData> consumePowerProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload).map(p -> mapper.fromProtoPowerConsumption(p));
    }

    @Incoming(CHANNEL_POWER_JSON_IN)
    @Outgoing(CHANNEL_POWER_OUT)
    @InterceptConsumingMessage(CHANNEL_POWER_JSON_IN)
    public Uni<PowerConsumptionData> consumePowerJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload).map(p -> mapper.fromJsonPowerConsumption(p));
    }
}
