package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

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
        super(CHANNEL_BATTERY_IN, dataMapper::toTuple, BatteryData::getTimestamp, sql);
    }

    @Incoming(CHANNEL_BATTERY_IN)
    public Uni<Void> consumeBattery(Message<List<BatteryData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}