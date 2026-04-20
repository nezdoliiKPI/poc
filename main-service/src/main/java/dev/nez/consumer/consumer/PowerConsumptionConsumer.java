package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;
import dev.nez.consumer.metrics.recorder.MetricsRecorder;
import dev.nez.proto.timeddata.PowerConsumptionData;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;

import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PowerConsumptionConsumer {
    private static final String CHANNEL_POWER_IN = "power-in";

    @Inject
    DataMapper dataMapper;

    @Inject
    MetricsRecorder recorder;

    @Inject
    Pool sqlClient;

    private final String sql = """
        INSERT INTO power_consumption (device_id, voltage, current, power, time_date)
        VALUES ($1, $2, $3, $4, $5)
    """;

    @Incoming(CHANNEL_POWER_IN)
    public Uni<Void> consumePowerProto(List<PowerConsumptionData> batch) {
        ArrayList<Tuple> tuples = new ArrayList<>(batch.size());
        for (var data : batch) tuples.add(dataMapper.toTuple(data));

        return sqlClient.withTransaction(
            conn -> conn.preparedQuery(sql).executeBatch(tuples)).replaceWithVoid()
            .eventually(() -> recorder.recordMessagesProcessed(CHANNEL_POWER_IN, batch.size()));
    }
}
