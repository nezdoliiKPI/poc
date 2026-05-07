package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.consumer.entity.PowerConsumptionEntity;
import dev.nez.consumer.metrics.MetricsRecorder;
import dev.nez.dto.proto.timeddata.BatteryData;
import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class PowerConsumptionConsumer extends BaseBatchConsumer<PowerConsumptionEntity> {
    private static final String CHANNEL_POWER_IN = "power-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO power_consumption (device_id, voltage, current, power, time_date)
        VALUES ($1, $2, $3, $4, $5)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    protected PowerConsumptionConsumer(MetricsRecorder recorder, Pool sqlClient) {
        super(recorder, sqlClient, CHANNEL_POWER_IN);
    }

    @Incoming(CHANNEL_POWER_IN)
    public Uni<Void> consumePower(Message<List<PowerConsumptionData>> batchMessage) {
        final var payload = batchMessage.getPayload().stream()
            .map(dataMapper::toEntity)
            .toList();

        return Uni.createFrom().item(batchMessage.withPayload(payload))
            .chain(lst -> consumeBatch(lst, sql, dataMapper::toTuple));
    }
}
