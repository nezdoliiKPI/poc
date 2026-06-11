package dev.nez.monitoring.dto.deserealizer;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.AlertData;
import dev.nez.monitoring.dto.Alert;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.UUID;

public class AlertDeserializer implements Deserializer<Alert> {
    @Override
    public Alert deserialize(String topic, byte[] data) {
        try {
            if (data != null && data.length > 0) {
                final AlertData alertData = AlertData.parseFrom(data);
                return new Alert(
                    UUID.fromString(alertData.getUuid()),
                    alertData.getDeviceId(),
                    alertData.getMetric(),
                    alertData.getValue(),
                    alertData.hasMin() ? alertData.getMin() : null,
                    alertData.hasMax() ? alertData.getMax() : null,
                    mapSeverity(alertData.getSeverity()),
                    alertData.getMessage(),
                    ProtoUtils.toInstant(alertData.getTimestamp())
                );
            } else {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error deserializing AlertData", e);
        }
    }

    private Alert.Severity mapSeverity(AlertData.Severity protoSeverity) {
        return switch (protoSeverity) {
            case FAULT -> Alert.Severity.FAULT;
            case WARNING -> Alert.Severity.WARNING;
            case CRITICAL -> Alert.Severity.CRITICAL;
            default -> throw new RuntimeException("Unknown Severity: " + protoSeverity);
        };
    }
}
