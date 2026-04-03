package dev.nez.edge.service.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.edge.data.AirQualityData;
import dev.nez.edge.dto.MessageMapper;
import dev.nez.edge.dto.mqtt.AirQuality;
import dev.nez.edge.dto.mqtt.AirQualityMessage;
import dev.nez.edge.exception.DecodeMessageException;
import dev.nez.edge.interceptor.RecordConsumingMessage;
import io.smallrye.mutiny.Uni;

import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class AirQualityService {
    private static final String CHANNEL_AIR_JSON = "air-j-in";
    private static final String CHANNEL_AIR_PROTO = "air-p-in";

    @Inject
    MessageMapper mapper;

    @Incoming(CHANNEL_AIR_JSON)
    @RecordConsumingMessage(CHANNEL_AIR_JSON)
    public Uni<AirQualityData> consumeAirQJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(Unchecked.function(p -> mapper.fromJsonAirQuality(p)));
    }

    @Incoming(CHANNEL_AIR_PROTO)
    @RecordConsumingMessage(CHANNEL_AIR_PROTO)
    public Uni<AirQualityData> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
            .map(Unchecked.function(p -> mapper.fromProtoAirQuality(p)));
    }
}