package dev.nez.edge.service.messaging;

import dev.nez.edge.dto.mqtt.Battery;
import dev.nez.edge.dto.mqtt.BatteryMessage;

import dev.nez.edge.service.metrics.Interceptor.RecordConsumingMessage;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class BatteryService {
    private static final String CHANNEL_BATT_JSON = "batt-j-in";
    private static final String CHANNEL_BATT_PROTO = "batt-p-in";

    @Incoming(CHANNEL_BATT_JSON)
    @RecordConsumingMessage(CHANNEL_BATT_JSON)
    public Uni<Battery> consumeTemperatureJson(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> Json.decodeValue(Buffer.buffer(p).getDelegate(), Battery.class))
                .invoke(telemetry -> Log.info("Received from json: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    @Incoming(CHANNEL_BATT_PROTO)
    @RecordConsumingMessage(CHANNEL_BATT_PROTO)
    public Uni<Battery> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(Unchecked.function(p -> BatteryMessage.parseFrom(p)))
                .map(msg -> new Battery(msg.getDeviceId(), msg.getVal()))
                .invoke(telemetry -> Log.info("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}
