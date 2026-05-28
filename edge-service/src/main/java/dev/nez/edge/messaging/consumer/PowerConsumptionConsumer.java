package dev.nez.edge.messaging.consumer;

import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import dev.nez.edge.dto.MessageDeserializer;

import io.smallrye.mutiny.Multi;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@Singleton
public class PowerConsumptionConsumer extends BaseMqttConsumer<PowerConsumptionData> {
    private static final String CHANNEL_POWER_JSON_IN = "power-j-in";
    private static final String CHANNEL_POWER_PROTO_IN = "power-p-in";
    private static final String CHANNEL_POWER_OUT = "power-out";

    private final ChannelFilter<PowerConsumptionData> jsonFilter;
    private final ChannelFilter<PowerConsumptionData> protoFilter;

    @Inject
    MessageDeserializer mapper;

    @Inject
    PowerConsumptionConsumer(MessageFilter messageFilter) {
        super(PowerConsumptionData::getDeviceId);

        BiFunction<PowerConsumptionData, PowerConsumptionData, Boolean> filter = (
            oldData,
            newData
        ) -> {
            final float VOLTAGE_DELTA = 1.0f;
            final float CURRENT_DELTA = 0.05f;
            final float POWER_DELTA = 5.0f;

            if (Math.abs(oldData.getVoltage() - newData.getVoltage()) >= VOLTAGE_DELTA) return true;
            if (Math.abs(oldData.getCurrent() - newData.getCurrent()) >= CURRENT_DELTA) return true;
            if (Math.abs(oldData.getPower() - newData.getPower()) >= POWER_DELTA) return true;

            return false;
        };

        protoFilter = messageFilter.newChannelFilter(filter, CHANNEL_POWER_PROTO_IN);
        jsonFilter = messageFilter.newChannelFilter(filter, CHANNEL_POWER_JSON_IN);
    }

    @Incoming(CHANNEL_POWER_PROTO_IN)
    @Outgoing(CHANNEL_POWER_OUT)
    public Multi<Message<PowerConsumptionData>> consumePowerProto(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromProtoPowerConsumption, protoFilter);
    }

    @Incoming(CHANNEL_POWER_JSON_IN)
    @Outgoing(CHANNEL_POWER_OUT)
    public Multi<Message<PowerConsumptionData>> consumePowerJson(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromJsonPowerConsumption, jsonFilter);
    }
}
