package dev.nez.edge.service.messaging;

import dev.nez.edge.data.BatteryData;
import dev.nez.edge.dto.MessageMapper;

import dev.nez.edge.interceptor.RecordConsumingMessage;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class BatteryService {
    private static final String CHANNEL_BATT_JSON = "batt-j-in";
    private static final String CHANNEL_BATT_PROTO = "batt-p-in";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_BATT_JSON)
    @RecordConsumingMessage(CHANNEL_BATT_JSON)
    public Uni<BatteryData> consumeTemperatureJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromJsonBattery(p));
    }

    @Incoming(CHANNEL_BATT_PROTO)
    @RecordConsumingMessage(CHANNEL_BATT_PROTO)
    public Uni<BatteryData> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromProtoBattery(p));
    }
}
