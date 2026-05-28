package dev.nez.consumer.consumer;

import dev.nez.consumer.data.DataMapper;

import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.TemperatureData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Singleton
public class TemperatureConsumer extends BaseBatchConsumer<TemperatureData> {
    private static final String CHANNEL_TEMP_IN = "temp-in";

    private static final String sql = """
        INSERT INTO temperature_data (device_id, temperature, humidity, time_date)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    @Inject
    TemperatureConsumer(DataMapper dataMapper) {
        final Function<TemperatureData, Instant> getInstant = data -> ProtoUtils.toInstant(data.getTimestamp());
        super(CHANNEL_TEMP_IN, dataMapper::toTuple, getInstant, sql);
    }

    @Incoming(CHANNEL_TEMP_IN)
    public Uni<Void> consumeTemp(Message<List<TemperatureData>> batchMessage) {
        return consumeBatch(batchMessage);
    }
}
