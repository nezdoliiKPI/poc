package dev.nez.edge.messaging.consumer;

import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;
import dev.nez.dto.proto.timeddata.TemperatureData;
import dev.nez.edge.dto.MessageDeserializer;

import io.smallrye.mutiny.Multi;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@Singleton
public class TemperatureConsumer extends BaseMqttConsumer<TemperatureData> {
    private static final String CHANNEL_TEMP_JSON_IN = "temp-j-in";
    private static final String CHANNEL_TEMP_PROTO_IN = "temp-p-in";
    private static final String CHANNEL_TEMP_OUT = "temp-out";

    private final ChannelFilter<TemperatureData> jsonFilter;
    private final ChannelFilter<TemperatureData> protoFilter;

    @Inject
    MessageDeserializer mapper;

    @Inject
    TemperatureConsumer(MessageFilter messageFilter) {
        super(TemperatureData::getDeviceId);

        BiFunction<TemperatureData, TemperatureData, Boolean> filter = (
            oldData,
            newData
        ) -> {
            final float TEMP_DELTA = 0.5f;
            final float HUMIDITY_DELTA = 0.5f;

            if (Math.abs(oldData.getTemperature() - newData.getTemperature()) >= TEMP_DELTA) return true;
            if (Math.abs(oldData.getHumidity() - newData.getHumidity()) >= HUMIDITY_DELTA) return true;

            return false;
        };

        this.jsonFilter = messageFilter.newChannelFilter(filter, CHANNEL_TEMP_JSON_IN);
        this.protoFilter = messageFilter.newChannelFilter(filter, CHANNEL_TEMP_PROTO_IN);
    }

    @Incoming(CHANNEL_TEMP_PROTO_IN)
    @Outgoing(CHANNEL_TEMP_OUT)
    public Multi<Message<TemperatureData>> consumeTempProto(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromProtoTemperature, protoFilter);
    }

    @Incoming(CHANNEL_TEMP_JSON_IN)
    @Outgoing(CHANNEL_TEMP_OUT)
    public Multi<Message<TemperatureData>> consumeTempJson(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromJsonTemperature, jsonFilter);
    }
}
