package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.consumer.entity.BatteryDataEntity;

import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class BatteryDataConsumer extends BaseBatchConsumer<BatteryDataEntity> {
    private static final String CHANNEL_BATTERY_IN = "batt-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO battery_data (device_id, val, time_date)
        VALUES ($1, $2, $3)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    BatteryDataConsumer() {
        super(CHANNEL_BATTERY_IN);
    }

    @Incoming(CHANNEL_BATTERY_IN)
    public Uni<Void> consumeBattery(Message<List<BatteryData>> batchMessage) {
        final var payload = batchMessage.getPayload().stream()
            .map(dataMapper::toEntity)
            .toList();

        return Uni.createFrom().item(batchMessage.withPayload(payload))
            .chain(lst -> consumeBatch(lst, sql, dataMapper::toTuple));
    }
}
