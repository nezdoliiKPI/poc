package dev.nez.edge.dto;

import com.google.protobuf.MessageLite;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.kafka.common.serialization.Serializer;

@RegisterForReflection
public class ProtobufSerializer<T extends MessageLite> implements Serializer<T> {

    @Override
    public byte[] serialize(String topic, T data) {
        return data != null
            ? data.toByteArray()
            : null;
    }
}
