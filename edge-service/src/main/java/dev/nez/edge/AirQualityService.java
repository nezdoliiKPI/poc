package dev.nez.edge;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.edge.dto.mqtt.AirQuality;
import dev.nez.edge.dto.mqtt.AirQualityMessage;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class AirQualityService {

    @Incoming("air-j-in")
    public Uni<AirQuality> consumeAirQJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> Json.decodeValue(Buffer.buffer(p).getDelegate(), AirQuality.class))
                .onItem().invoke(telemetry -> Log.debug("Received from json: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    @Incoming("air-p-in")
    public Uni<AirQuality> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> {
                    try {
                        return AirQualityMessage.parseFrom(p);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(msg -> new AirQuality(
                        msg.getDeviceId(),
                        msg.getCo2(),
                        msg.getPm25(),
                        msg.getPm10(),
                        msg.getTvoc(),
                        msg.getTemperature(),
                        msg.getHumidity()
                ))
                .onItem().invoke(telemetry -> Log.debug("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}