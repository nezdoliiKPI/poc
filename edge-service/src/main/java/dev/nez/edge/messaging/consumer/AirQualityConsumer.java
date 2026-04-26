package dev.nez.edge.messaging.consumer;

import dev.nez.edge.interceptor.InterceptConsumingMessage;
import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.messaging.filter.MessageFilter.ChannelFilter;
import dev.nez.proto.timeddata.AirQualityData;
import dev.nez.edge.dto.MessageMapper;

import io.smallrye.mutiny.Multi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.function.BiFunction;

@ApplicationScoped
public class AirQualityConsumer {
    private static final String CHANNEL_AIR_JSON_IN = "air-j-in";
    private static final String CHANNEL_AIR_PROTO_IN = "air-p-in";
    private static final String CHANNEL_AIR_OUT = "air-out";

    private final ChannelFilter<AirQualityData> jsonFilter;
    private final ChannelFilter<AirQualityData> protoFilter;

    private final BiFunction<AirQualityData, AirQualityData, Boolean> filter = (
        oldData,
        newData
    ) -> {
        final int CO2_DELTA = 10;
        final float PM25_DELTA = 2.0f;
        final float PM10_DELTA = 5.0f;
        final float TVOC_DELTA = 0.1f;
        final float TEMP_DELTA = 0.5f;
        final float HUMIDITY_DELTA = 2.0f;

        if (Math.abs(oldData.getCo2() - newData.getCo2()) >= CO2_DELTA) return true;
        if (Math.abs(oldData.getTemperature() - newData.getTemperature()) >= TEMP_DELTA) return true;
        if (Math.abs(oldData.getHumidity() - newData.getHumidity()) >= HUMIDITY_DELTA) return true;
        if (Math.abs(oldData.getPm25() - newData.getPm25()) >= PM25_DELTA) return true;
        if (Math.abs(oldData.getPm10() - newData.getPm10()) >= PM10_DELTA) return true;
        if (Math.abs(oldData.getTvoc() - newData.getTvoc()) >= TVOC_DELTA) return true;

        return false;
    };

    @Inject
    MessageMapper mapper;

    @Inject
    AirQualityConsumer(MessageFilter messageFilter) {
        this.jsonFilter = messageFilter.newChannelFilter(filter, CHANNEL_AIR_JSON_IN);
        this.protoFilter = messageFilter.newChannelFilter(filter, CHANNEL_AIR_PROTO_IN);
    }

    @Incoming(CHANNEL_AIR_PROTO_IN)
    @Outgoing(CHANNEL_AIR_OUT)
    @InterceptConsumingMessage(CHANNEL_AIR_PROTO_IN)
    public Multi<AirQualityData> consumeAirProto(Multi<byte[]> stream) {
        return stream
            .filter(_ -> protoFilter.shouldConsume())
            .map(mapper::fromProtoAirQuality)
            .filter(data -> protoFilter.apply(data.getDeviceId(), data));
    }

    @Incoming(CHANNEL_AIR_JSON_IN)
    @Outgoing(CHANNEL_AIR_OUT)
    @InterceptConsumingMessage(CHANNEL_AIR_JSON_IN)
    public Multi<AirQualityData> consumeAirQJson(Multi<byte[]> stream) {
        return stream
            .filter(_ -> jsonFilter.shouldConsume())
            .map(mapper::fromJsonAirQuality)
            .filter(data -> jsonFilter.apply(data.getDeviceId(), data));
    }
}