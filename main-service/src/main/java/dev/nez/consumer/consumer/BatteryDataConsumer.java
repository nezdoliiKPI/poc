package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.consumer.entity.BatteryDataEntity;
import dev.nez.dto.proto.timeddata.BatteryData;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;

@ApplicationScoped
public class BatteryDataConsumer extends BaseBatchConsumer<BatteryDataEntity> {
    private static final String CHANNEL_BATTERY_IN = "batt-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO battery_data (device_id, val, time_date)
        VALUES ($1, $2, $3)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Incoming(CHANNEL_BATTERY_IN)
    public Uni<Void> consumeBattery(List<BatteryData> batch) {
        return Uni.createFrom()
            .item(batch.stream().map(dataMapper::toEntity).toList())
            .chain(lst -> consumeBatch(lst, sql, CHANNEL_BATTERY_IN, dataMapper::toTuple));
    }
}
