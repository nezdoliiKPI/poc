package dev.nez.consumer.deserealizer;

import dev.nez.dto.proto.timeddata.SmokeDetectorData;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SmokeDetectorDeserializer implements Deserializer<SmokeDetectorData> {
    @Override
    public SmokeDetectorData deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : SmokeDetectorData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing SmokeDetectorData", e);
        }
    }
}
