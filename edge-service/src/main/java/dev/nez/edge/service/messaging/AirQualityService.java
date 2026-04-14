package dev.nez.edge.service.messaging;

import dev.nez.proto.timeddata.AirQualityData;
import dev.nez.edge.dto.MessageMapper;

import dev.nez.edge.interceptor.InterceptConsumingMessage;

import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class AirQualityService {
    private static final String CHANNEL_AIR_JSON_IN = "air-j-in";
    private static final String CHANNEL_AIR_PROTO_IN = "air-p-in";
    private static final String CHANNEL_AIR_OUT = "air-out";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_AIR_JSON_IN)
    @Outgoing(CHANNEL_AIR_OUT)
    @InterceptConsumingMessage(CHANNEL_AIR_JSON_IN)
    public Uni<AirQualityData> consumeAirQJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromJsonAirQuality(p));
    }

    @Incoming(CHANNEL_AIR_PROTO_IN)
    @Outgoing(CHANNEL_AIR_OUT)
    @InterceptConsumingMessage(CHANNEL_AIR_PROTO_IN)
    public Uni<AirQualityData> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(p -> mapper.fromProtoAirQuality(p));
    }
}