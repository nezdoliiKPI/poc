package dev.nez.dto.proto;

import java.time.Instant;

public final class ProtoUtils {
    private ProtoUtils() {}

    public static Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}
