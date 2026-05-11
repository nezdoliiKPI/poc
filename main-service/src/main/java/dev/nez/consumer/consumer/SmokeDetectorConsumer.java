package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class SmokeDetectorConsumer extends BaseBatchConsumer<SmokeDetectorData> {
    private static final String CHANNEL_SMOKE_IN = "smoke-in";

    private static final String sql = """
        INSERT INTO smoke_detector (device_id, smoke_raw, co_level, time_date)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    SmokeDetectorConsumer(DataMapper dataMapper) {
        super(CHANNEL_SMOKE_IN, dataMapper::toTuple, SmokeDetectorData::getTimestamp, sql);
    }

    @Incoming(CHANNEL_SMOKE_IN)
    public Uni<Void> consumeSmoke(Message<List<SmokeDetectorData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}
