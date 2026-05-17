package dev.nez.alert;

import java.util.List;

public record Alert(
    Long deviceId,
    List<String> messages
) {
}
