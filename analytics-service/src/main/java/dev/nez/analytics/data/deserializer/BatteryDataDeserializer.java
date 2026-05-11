package dev.nez.analytics.data.deserializer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.BatteryData;
import org.apache.kafka.common.serialization.Deserializer;

public class BatteryDataDeserializer implements Deserializer<BatteryData> {
    @Override
    public BatteryData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : BatteryData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing BatteryData", e);
        }
    }
}
