package dev.nez.edge;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.edge.dto.mqtt.TemperatureHumidity;
import dev.nez.edge.dto.mqtt.TemperatureHumidityMessage;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class TemperatureService {

    @Incoming("temp-out")
    @Merge
    public  Uni<Void> trash(TemperatureHumidity telemetry) {
        return  Uni.createFrom().nullItem();
    }

    @Incoming("temp-j-in")
    @Outgoing("temp-out")
    public Uni<TemperatureHumidity> consumeTemperatureJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> Json.decodeValue(Buffer.buffer(p).getDelegate(), TemperatureHumidity.class))
                .onItem().invoke(telemetry -> Log.info("Received from json: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    @Incoming("temp-p-in")
    @Outgoing("temp-out")
    public Uni<TemperatureHumidity> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> {
                    try {
                        return TemperatureHumidityMessage.parseFrom(p);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(msg -> new TemperatureHumidity(msg.getDeviceId(), msg.getTemp(), msg.getHumidity()))
                .onItem().invoke(telemetry -> Log.info("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}