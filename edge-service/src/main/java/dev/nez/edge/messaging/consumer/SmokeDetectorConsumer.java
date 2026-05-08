package dev.nez.edge.messaging.consumer;

import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import dev.nez.edge.dto.MessageMapper;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@Singleton
public class SmokeDetectorConsumer extends BaseMqttConsumer<SmokeDetectorData> {
    private static final String CHANNEL_SMOKE_JSON_IN = "smoke-j-in";
    private static final String CHANNEL_SMOKE_PROTO_IN = "smoke-p-in";
    private static final String CHANNEL_SMOKE_OUT = "smoke-out";

    private final ChannelFilter<SmokeDetectorData> jsonFilter;
    private final ChannelFilter<SmokeDetectorData> protoFilter;

    @Inject
    MessageMapper mapper;

    @Inject
    SmokeDetectorConsumer(MessageFilter messageFilter) {
        super(SmokeDetectorData::getDeviceId);

        BiFunction<SmokeDetectorData, SmokeDetectorData, Boolean> filter = (
            oldData,
            newData
        ) -> {
            final int SMOKE_RAW_DELTA = 10;
            final int CO_PPM_DELTA = 1;

            if (Math.abs(oldData.getSmokeRaw() - newData.getSmokeRaw()) >= SMOKE_RAW_DELTA) return true;
            if (Math.abs(oldData.getCoLevel() - newData.getCoLevel()) >= CO_PPM_DELTA) return true;

            return false;
        };

        protoFilter = messageFilter.newChannelFilter(filter, CHANNEL_SMOKE_PROTO_IN);
        jsonFilter = messageFilter.newChannelFilter(filter, CHANNEL_SMOKE_JSON_IN);
    }

    @Incoming(CHANNEL_SMOKE_PROTO_IN)
    @Outgoing(CHANNEL_SMOKE_OUT)
    public Multi<Message<SmokeDetectorData>> consumeSmokeDetectorProto(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromProtoSmoke, protoFilter, CHANNEL_SMOKE_PROTO_IN);
    }

    @Incoming(CHANNEL_SMOKE_JSON_IN)
    @Outgoing(CHANNEL_SMOKE_OUT)
    public Multi<Message<SmokeDetectorData>> consumeSmokeDetectorJson(Multi<Message<byte[]>> stream) {
        return consume(stream, mapper::fromJsonSmoke, jsonFilter, CHANNEL_SMOKE_JSON_IN);
    }
}
