package dev.nez.dto.proto;

import java.time.Instant;

public final class ProtoUtils {
    private ProtoUtils() {}

    public static Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    public static com.google.protobuf.Timestamp toTimestamp(Instant instant) {
        return com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
}
