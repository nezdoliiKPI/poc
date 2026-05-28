package dev.nez.consumer.consumer;

import dev.nez.consumer.data.DataMapper;

import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Singleton
public class BatteryDataConsumer extends BaseBatchConsumer<BatteryData> {
    private static final String CHANNEL_BATTERY_IN = "batt-in";

    private static final String sql = """
        INSERT INTO battery_data (device_id, val, time_date)
        VALUES ($1, $2, $3)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    BatteryDataConsumer(DataMapper dataMapper) {
        final Function<BatteryData, Instant> getInstant = data -> ProtoUtils.toInstant(data.getTimestamp());
        super(CHANNEL_BATTERY_IN, dataMapper::toTuple, getInstant, sql);
    }

    @Incoming(CHANNEL_BATTERY_IN)
    public Uni<Void> consumeBattery(Message<List<BatteryData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}