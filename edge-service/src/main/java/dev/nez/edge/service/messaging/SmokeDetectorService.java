package dev.nez.edge.service.messaging;

import dev.nez.edge.data.SmokeDetectorData;
import dev.nez.edge.dto.MessageMapper;
import dev.nez.edge.interceptor.RecordConsumingMessage;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class SmokeDetectorService {
    private static final String CHANNEL_SMOKE_PROTO_IN = "smoke-p-in";
    private static final String CHANNEL_SMOKE_OUT = "smoke-out";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_SMOKE_PROTO_IN)
    @Outgoing(CHANNEL_SMOKE_OUT)
    @RecordConsumingMessage(CHANNEL_SMOKE_PROTO_IN)
    public Uni<SmokeDetectorData> consumeSmokeDetectorProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromProtoSmoke(p));
    }
}
