package dev.nez.edge.service.messaging;

import dev.nez.proto.timeddata.BatteryData;
import dev.nez.edge.dto.MessageMapper;
import dev.nez.edge.interceptor.InterceptConsumingMessage;

import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class BatteryConsumer {
    private static final String CHANNEL_BATT_JSON_IN = "batt-j-in";
    private static final String CHANNEL_BATT_PROTO_IN = "batt-p-in";
    private static final String CHANNEL_BATT_OUT = "batt-out";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_BATT_JSON_IN)
    @Outgoing(CHANNEL_BATT_OUT)
    @InterceptConsumingMessage(CHANNEL_BATT_JSON_IN)
    public Uni<BatteryData> consumeTemperatureJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload).map(p -> mapper.fromJsonBattery(p));
    }

    @Incoming(CHANNEL_BATT_PROTO_IN)
    @Outgoing(CHANNEL_BATT_OUT)
    @InterceptConsumingMessage(CHANNEL_BATT_PROTO_IN)
    public Uni<BatteryData> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload).map(p -> mapper.fromProtoBattery(p));
    }
}
