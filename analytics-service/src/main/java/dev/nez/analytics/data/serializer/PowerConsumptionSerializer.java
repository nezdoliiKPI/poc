package dev.nez.analytics.data.serializer;

import dev.nez.dto.proto.timeddata.PowerConsumptionData;
import org.apache.kafka.common.serialization.Serializer;

public class PowerConsumptionSerializer  implements Serializer<PowerConsumptionData> {
    @Override
    public byte[] serialize(String topic, PowerConsumptionData data) {
        return data == null ? null : data.toByteArray();
    }
}
