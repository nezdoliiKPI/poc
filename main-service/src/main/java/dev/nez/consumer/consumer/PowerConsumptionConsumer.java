package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.proto.timeddata.PowerConsumptionData;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;

@ApplicationScoped
public class PowerConsumptionConsumer extends BaseBatchConsumer<PowerConsumptionData> {
    private static final String CHANNEL_POWER_IN = "power-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO power_consumption (device_id, voltage, current, power, time_date)
        VALUES ($1, $2, $3, $4, $5)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Incoming(CHANNEL_POWER_IN)
    public Uni<Void> consumePower(List<PowerConsumptionData> batch) {
        return consumeBatch(batch, sql, CHANNEL_POWER_IN, dataMapper::toTuple);
    }
}
