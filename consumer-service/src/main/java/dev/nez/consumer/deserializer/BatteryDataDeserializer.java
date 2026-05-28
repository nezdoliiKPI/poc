package dev.nez.consumer.deserializer;

import dev.nez.dto.proto.timeddata.BatteryData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;

public class BatteryDataDeserializer implements Deserializer<BatteryData> {
    @Override
    public BatteryData deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? BatteryData.parseFrom(data) : null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing BatteryData", e);
        }
    }
}
