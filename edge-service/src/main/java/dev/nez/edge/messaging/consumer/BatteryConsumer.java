package dev.nez.edge.messaging.consumer;

import dev.nez.edge.interceptor.InterceptConsumingMessage;
import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;
import dev.nez.proto.timeddata.BatteryData;
import dev.nez.edge.dto.MessageMapper;

import io.smallrye.mutiny.Multi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@ApplicationScoped
public class BatteryConsumer {
    private static final String CHANNEL_BATT_JSON_IN = "batt-j-in";
    private static final String CHANNEL_BATT_PROTO_IN = "batt-p-in";
    private static final String CHANNEL_BATT_OUT = "batt-out";

    private final ChannelFilter<BatteryData> jsonFilter;
    private final ChannelFilter<BatteryData> protoFilter;

    @Inject
    MessageMapper mapper;

    @Inject
    BatteryConsumer(MessageFilter messageFilter) {
        BiFunction<BatteryData, BatteryData, Boolean> filter = (
            oldData,
            newData
        ) -> {
            final float BATTERY_PERCENT_DELTA = 0.5f;
            return Math.abs(oldData.getVal() - newData.getVal()) >= BATTERY_PERCENT_DELTA;
        };

        protoFilter = messageFilter.newChannelFilter(filter, CHANNEL_BATT_JSON_IN);
        jsonFilter = messageFilter.newChannelFilter(filter, CHANNEL_BATT_PROTO_IN);
    }

    @Incoming(CHANNEL_BATT_PROTO_IN)
    @Outgoing(CHANNEL_BATT_OUT)
    @InterceptConsumingMessage(CHANNEL_BATT_PROTO_IN)
    public Multi<BatteryData> consumeBatteryProto(Multi<byte[]> stream) {
        return stream
            .filter(_ -> protoFilter.shouldConsume())
            .map(mapper::fromProtoBattery)
            .filter(data -> protoFilter.apply(data.getDeviceId(), data));
    }

    @Incoming(CHANNEL_BATT_JSON_IN)
    @Outgoing(CHANNEL_BATT_OUT)
    @InterceptConsumingMessage(CHANNEL_BATT_JSON_IN)
    public Multi<BatteryData> consumeBatteryJson(Multi<byte[]> stream) {
        return stream
            .filter(_ -> jsonFilter.shouldConsume())
            .map(mapper::fromJsonBattery)
            .filter(data -> jsonFilter.apply(data.getDeviceId(), data));
    }
}
