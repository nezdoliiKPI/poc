package dev.nez.edge.service.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
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

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class AirQualityService {
    private static final String CHANNEL_AIR_JSON = "air-j-in";
    private static final String CHANNEL_AIR_PROTO = "air-p-in";

    @Incoming(CHANNEL_AIR_JSON)
    @RecordConsumingMessage(CHANNEL_AIR_JSON)
    public Uni<AirQuality> consumeAirQJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(Unchecked.function(p -> {
                    try {
                        return Json.decodeValue(Buffer.buffer(p).getDelegate(), AirQuality.class);
                    } catch (final DecodeException e) {
                        throw new DecodeMessageException(e.getMessage(), e);
                    }
                }));
    }

    @Incoming(CHANNEL_AIR_PROTO)
    @RecordConsumingMessage(CHANNEL_AIR_PROTO)
    public Uni<AirQuality> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(Unchecked.function(p -> {
                    try {
                        return AirQualityMessage.parseFrom(p);
                    } catch (final InvalidProtocolBufferException e) {
                        throw new DecodeMessageException(e.getMessage(), e);
                    }
                }))
                .map(msg -> new AirQuality(
                        msg.getDeviceId(),
                        msg.getCo2(),
                        msg.getPm25(),
                        msg.getPm10(),
                        msg.getTvoc(),
                        msg.getTemperature(),
                        msg.getHumidity()
                ));
    }
}