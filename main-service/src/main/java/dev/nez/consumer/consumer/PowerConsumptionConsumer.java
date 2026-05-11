package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class PowerConsumptionConsumer extends BaseBatchConsumer<PowerConsumptionData> {
    private static final String CHANNEL_POWER_IN = "power-in";

    private static final String sql = """
        INSERT INTO power_consumption (device_id, voltage, current, power, time_date)
        VALUES ($1, $2, $3, $4, $5)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    PowerConsumptionConsumer(DataMapper dataMapper) {
        super(CHANNEL_POWER_IN, dataMapper::toTuple, PowerConsumptionData::getTimestamp, sql);
    }

    @Incoming(CHANNEL_POWER_IN)
    public Uni<Void> consumePower(Message<List<PowerConsumptionData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}
