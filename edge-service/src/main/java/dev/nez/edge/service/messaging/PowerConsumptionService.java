package dev.nez.edge.service.messaging;

import dev.nez.edge.data.PowerConsumptionData;
import dev.nez.edge.dto.MessageMapper;

import dev.nez.edge.interceptor.RecordConsumingMessage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class PowerConsumptionService {
    private static final String CHANNEL_POWER_PROTO = "power-p-in";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_POWER_PROTO)
    @RecordConsumingMessage(CHANNEL_POWER_PROTO)
    public Uni<PowerConsumptionData> consumePowerProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(Unchecked.function(p -> mapper.fromProtoPowerConsumption(p)));
    }
}
