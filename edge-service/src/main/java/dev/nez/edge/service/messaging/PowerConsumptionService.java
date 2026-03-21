package dev.nez.edge.service.messaging;

import dev.nez.edge.dto.mqtt.PowerConsumption;
import dev.nez.edge.dto.mqtt.PowerConsumptionMessage;
import dev.nez.edge.service.metrics.MetricsRecorder;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class PowerConsumptionService {
    private static final String CHANNEL_POWER_PROTO = "power-p-in";

    @Inject
    MetricsRecorder recorder;

    @Incoming(CHANNEL_POWER_PROTO)
    public Uni<PowerConsumption> consumeAirQProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .invoke(() -> recorder.recordMqttMessageReceived(CHANNEL_POWER_PROTO))
                .map(Unchecked.function(p -> PowerConsumptionMessage.parseFrom(p)))
                .map(msg -> new PowerConsumption(
                        msg.getDeviceId(),
                        msg.getVoltage(),
                        msg.getCurrent(),
                        msg.getPower()
                ))
                .invoke(telemetry -> Log.debug("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }
}
