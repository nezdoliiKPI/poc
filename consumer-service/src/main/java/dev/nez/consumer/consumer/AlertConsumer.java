package dev.nez.consumer.consumer;

import dev.nez.consumer.data.Alert;
import dev.nez.consumer.data.DataMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class AlertConsumer  extends BaseBatchConsumer<Alert> {
    private static final String CHANNEL_ALERT_IN = "alert-in";

    private static final String sql = """
        INSERT INTO alerts (alert_uuid, device_id, metric, value, min_threshold, max_threshold, severity, message, time_date) 
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) 
        ON CONFLICT (alert_uuid, time_date) DO NOTHING
    """;

    @Inject
    AlertConsumer(DataMapper dataMapper) {
        super(CHANNEL_ALERT_IN, dataMapper::toTuple, Alert::ts, sql);
    }

    @Incoming(CHANNEL_ALERT_IN)
    public Uni<Void> consumeAir(Message<List<Alert>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}
