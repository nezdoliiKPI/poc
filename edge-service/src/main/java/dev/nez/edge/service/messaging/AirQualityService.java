package dev.nez.edge.service.messaging;

import dev.nez.edge.dto.mqtt.AirQuality;
import dev.nez.edge.dto.mqtt.AirQualityMessage;
import dev.nez.edge.service.metrics.Interceptor.RecordConsumingMessage;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class AirQualityService {
    private static final String CHANNEL_AIR_JSON = "air-j-in";
    private static final String CHANNEL_AIR_PROTO = "air-p-in";

    @Incoming(CHANNEL_AIR_JSON)
    @RecordConsumingMessage(topic = CHANNEL_AIR_JSON)
    public Uni<AirQuality> consumeAirQJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> Json.decodeValue(Buffer.buffer(p).getDelegate(), AirQuality.class))
                .invoke(telemetry -> Log.debug("Received from json: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    @Incoming(CHANNEL_AIR_PROTO)
    @RecordConsumingMessage(topic = CHANNEL_AIR_PROTO)
    public Uni<AirQuality> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(Unchecked.function(p -> AirQualityMessage.parseFrom(p)))
                .map(msg -> new AirQuality(
                        msg.getDeviceId(),
                        msg.getCo2(),
                        msg.getPm25(),
                        msg.getPm10(),
                        msg.getTvoc(),
                        msg.getTemperature(),
                        msg.getHumidity()
                ))
                .invoke(telemetry -> Log.debug("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}