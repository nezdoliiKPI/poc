package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.consumer.entity.SmokeDetectorEntity;
import dev.nez.consumer.metrics.MetricsRecorder;
import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class SmokeDetectorConsumer extends BaseBatchConsumer<SmokeDetectorEntity> {
    private static final String CHANNEL_SMOKE_IN = "smoke-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO smoke_detector (device_id, smoke_raw, co_level, time_date)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    protected SmokeDetectorConsumer(MetricsRecorder recorder, Pool sqlClient) {
        super(recorder, sqlClient, CHANNEL_SMOKE_IN );
    }

    @Incoming(CHANNEL_SMOKE_IN)
    public Uni<Void> consumeSmoke(Message<List<SmokeDetectorData>> batchMessage) {
        final var payload = batchMessage.getPayload().stream()
            .map(dataMapper::toEntity)
            .toList();

        return Uni.createFrom().item(batchMessage.withPayload(payload))
            .chain(lst -> consumeBatch(lst, sql, dataMapper::toTuple));
    }
}
