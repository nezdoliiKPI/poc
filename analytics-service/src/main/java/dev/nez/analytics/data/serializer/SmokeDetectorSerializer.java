package dev.nez.analytics.data.serializer;

import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import org.apache.kafka.common.serialization.Serializer;

public class SmokeDetectorSerializer  implements Serializer<SmokeDetectorData> {
    @Override
    public byte[] serialize(String topic, SmokeDetectorData data) {
        return data == null ? null : data.toByteArray();
    }
}
