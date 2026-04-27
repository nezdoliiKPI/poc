package dev.nez.edge.messaging.consumer;

import dev.nez.edge.interceptor.InterceptConsumingMessage;
import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;
import dev.nez.proto.timeddata.SmokeDetectorData;
import dev.nez.edge.dto.MessageMapper;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@ApplicationScoped
public class SmokeDetectorConsumer {
    private static final String CHANNEL_SMOKE_JSON_IN = "smoke-j-in";
    private static final String CHANNEL_SMOKE_PROTO_IN = "smoke-p-in";
    private static final String CHANNEL_SMOKE_OUT = "smoke-out";

    private final ChannelFilter<SmokeDetectorData> jsonFilter;
    private final ChannelFilter<SmokeDetectorData> protoFilter;

    @Inject
    MessageMapper mapper;

    @Inject
    SmokeDetectorConsumer(MessageFilter messageFilter) {
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
    @InterceptConsumingMessage(CHANNEL_SMOKE_PROTO_IN)
    public Multi<SmokeDetectorData> consumeSmokeDetectorProto(Multi<byte[]> stream) {
        return stream
            .filter(_ -> protoFilter.shouldConsume())
            .map(mapper::fromProtoSmoke)
            .filter(data -> protoFilter.apply(data.getDeviceId(), data));
    }

    @Incoming(CHANNEL_SMOKE_JSON_IN)
    @Outgoing(CHANNEL_SMOKE_OUT)
    @InterceptConsumingMessage(CHANNEL_SMOKE_JSON_IN)
    public Multi<SmokeDetectorData> consumeSmokeDetectorJson(Multi<byte[]> stream) {
        return stream
            .filter(_ -> jsonFilter.shouldConsume())
            .map(mapper::fromJsonSmoke)
            .filter(data -> jsonFilter.apply(data.getDeviceId(), data));
    }
}
