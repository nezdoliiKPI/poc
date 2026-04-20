package dev.nez.consumer.consumer;

import dev.nez.consumer.DataMapper;
import dev.nez.consumer.interceptor.InterceptConsumingMessage;
import dev.nez.proto.timeddata.SmokeDetectorData;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class SmokeDetectorConsumer {
    private static final String CHANNEL_SMOKE_IN = "smoke-in";

    @Inject
    DataMapper dataMapper;

    @Incoming(CHANNEL_SMOKE_IN)
    @InterceptConsumingMessage(CHANNEL_SMOKE_IN)
    public Uni<SmokeDetectorData> consumePowerProto(SmokeDetectorData data) {
        return Uni.createFrom().item(data)
            .call(air -> Panache.withTransaction(() -> dataMapper.toEntity(air).persist()));
    }
}
