package dev.nez.edge.messaging.consumer;

import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;

import dev.nez.dto.proto.timeddata.BatteryData;
import dev.nez.edge.dto.MessageDeserializer;

import io.smallrye.mutiny.Multi;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@Singleton
public class BatteryConsumer extends BaseMqttConsumer<BatteryData> {
    private static final String CHANNEL_BATT_JSON_IN = "batt-j-in";
    private static final String CHANNEL_BATT_PROTO_IN = "batt-p-in";
    private static final String CHANNEL_BATT_OUT = "batt-out";

    private final ChannelFilter<BatteryData> jsonFilter;
    private final ChannelFilter<BatteryData> protoFilter;

    @Inject
    MessageDeserializer mapper;

    @Inject
    BatteryConsumer(MessageFilter messageFilter) {
        super(BatteryData::getDeviceId);

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
    public Multi<Message<BatteryData>> consumeBatteryProto(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromProtoBattery, protoFilter);
    }

    @Incoming(CHANNEL_BATT_JSON_IN)
    @Outgoing(CHANNEL_BATT_OUT)
    public Multi<Message<BatteryData>> consumeBatteryJson(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromJsonBattery, jsonFilter);
    }
}
