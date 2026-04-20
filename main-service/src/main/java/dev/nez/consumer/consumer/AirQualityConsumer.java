package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;
import dev.nez.consumer.interceptor.InterceptConsumingMessage;
import dev.nez.proto.timeddata.AirQualityData;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class AirQualityConsumer {
    private static final String CHANNEL_AIR_IN = "air-in";

    @Inject
    DataMapper dataMapper;

    @Incoming(CHANNEL_AIR_IN)
    @InterceptConsumingMessage(CHANNEL_AIR_IN)
    public Uni<AirQualityData> consumeAirProto(AirQualityData data) {
        return Uni.createFrom().item(data)
            .call(air -> Panache.withTransaction(() -> dataMapper.toEntity(air).persist()));
    }
}
