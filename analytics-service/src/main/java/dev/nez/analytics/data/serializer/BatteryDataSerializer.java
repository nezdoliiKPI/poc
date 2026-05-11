package dev.nez.analytics.data.serializer;

import dev.nez.dto.proto.timeddata.BatteryData;
import org.apache.kafka.common.serialization.Serializer;

public class BatteryDataSerializer implements Serializer<BatteryData> {
    @Override
    public byte[] serialize(String topic, BatteryData data) {
        return data == null ? null : data.toByteArray();
    }
}
