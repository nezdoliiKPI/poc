package dev.nez.consumer.deserializer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.timeddata.AlertData;
import org.apache.kafka.common.serialization.Deserializer;

public class AlertDeserializer implements Deserializer<AlertData> {
    @Override
    public AlertData deserialize(String topic, byte[] data) {
        try {
            return (data != null && data.length > 0) ? AlertData.parseFrom(data) : null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing AlertData", e);
        }
    }
}
