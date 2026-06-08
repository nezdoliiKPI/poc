package dev.nez.consumer.consumer;

import dev.nez.consumer.data.DataMapper;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.AlertData;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Singleton
public class AlertConsumer  extends BaseBatchConsumer<AlertData> {
    private static final String CHANNEL_ALERT_IN = "alert-in";

    private static final String sql = """
        INSERT INTO alerts (alert_uuid, device_id, metric, value, min_threshold, max_threshold, severity, message, time_date) 
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) 
        ON CONFLICT (alert_uuid, time_date) DO NOTHING
    """;

    @Inject
    AlertConsumer(DataMapper dataMapper) {
        final Function<AlertData, Instant> getInstant = data -> ProtoUtils.toInstant(data.getTimestamp());
        super(CHANNEL_ALERT_IN, dataMapper::toTuple, getInstant, sql);
    }

    @Incoming(CHANNEL_ALERT_IN)
    public Uni<Void> consumeAir(Message<List<AlertData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}
