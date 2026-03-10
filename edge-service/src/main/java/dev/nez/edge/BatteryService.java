package dev.nez.edge;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.edge.dto.mqtt.Battery;
import dev.nez.edge.dto.mqtt.BatteryMessage;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class BatteryService {

    @Incoming("batt-out")
    @Merge
    public Uni<Void> trash(Battery telemetry) {
        return  Uni.createFrom().nullItem();
    }

    @Incoming("batt-j-in")
    @Outgoing("batt-out")
    public Uni<Battery> consumeTemperatureJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> Json.decodeValue(Buffer.buffer(p).getDelegate(), Battery.class))
                .onItem().invoke(telemetry -> Log.info("Received from json: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    @Incoming("batt-p-in")
    @Outgoing("batt-out")
    public Uni<Battery> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> {
                    try {
                        return BatteryMessage.parseFrom(p);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(msg -> new Battery(msg.getDeviceId(), msg.getVal()))
                .onItem().invoke(telemetry -> Log.info("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}
