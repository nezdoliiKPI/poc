package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;

import dev.nez.proto.timeddata.AirQualityData;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.List;

@ApplicationScoped
public class AirQualityConsumer extends BaseBatchConsumer<AirQualityData> {
    private static final String CHANNEL_AIR_IN = "air-in";

    @Inject
    DataMapper dataMapper;

    private static final String sql = """
        INSERT INTO air_quality (device_id, co2, pm25, pm10, tvoc, temperature, humidity, time_date)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    """;

    @Incoming(CHANNEL_AIR_IN)
    public Uni<Void> consumeAir(List<AirQualityData>batch) {
        return consumeBatch(batch, sql, CHANNEL_AIR_IN, dataMapper::toTuple);
    }
}
