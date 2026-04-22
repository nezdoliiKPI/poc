package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.proto.timeddata.SmokeDetectorData;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;

@ApplicationScoped
public class SmokeDetectorConsumer extends BaseBatchConsumer<SmokeDetectorData> {
    private static final String CHANNEL_SMOKE_IN = "smoke-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO smoke_detector (device_id, smoke_raw, co_level, time_date)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Incoming(CHANNEL_SMOKE_IN)
    public Uni<Void> consumeSmoke(List<SmokeDetectorData> batch) {
        return consumeBatch(batch, sql, CHANNEL_SMOKE_IN, dataMapper::toTuple);
    }
}
