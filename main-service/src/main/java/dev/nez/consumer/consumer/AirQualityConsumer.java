package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.consumer.entity.AirQualityEntity;
import dev.nez.dto.proto.timeddata.AirQualityData;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@Singleton
public class AirQualityConsumer extends BaseBatchConsumer<AirQualityEntity> {
    private static final String CHANNEL_AIR_IN = "air-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO air_quality (device_id, co2, pm25, pm10, tvoc, temperature, humidity, time_date)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
        ON CONFLICT (device_id, time_date) DO NOTHING
    """;

    AirQualityConsumer() {
        super(CHANNEL_AIR_IN);
    }

    @Incoming(CHANNEL_AIR_IN)
    public Uni<Void> consumeAir(Message<List<AirQualityData>> batchMessage) {
        final var payload = batchMessage.getPayload().stream()
            .map(dataMapper::toEntity)
            .toList();

        return Uni.createFrom().item(batchMessage.withPayload(payload))
            .chain(lst -> consumeBatch(lst, sql, dataMapper::toTuple));
    }
}
