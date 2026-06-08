package dev.nez.analytics.data.alert;

import dev.nez.dto.proto.ProtoUtils;
import dev.nez.dto.proto.timeddata.AlertData;
import org.apache.kafka.common.serialization.Serializer;

public class AlertSerializer implements Serializer<Alert> {

    @Override
    public byte[] serialize(String topic, Alert data) {
        if (data != null) {
            final var builder = AlertData.newBuilder()
                .setUuid(data.uuid().toString())
                .setDeviceId(data.dID())
                .setMetric(data.metric())
                .setValue(data.val())
                .setSeverity(mapSeverity(data.sev()))
                .setMessage(data.msg())
                .setTimestamp(ProtoUtils.toTimestamp(data.ts()));

            if (data.min() != null) {
                builder.setMin(data.min());
            }
            if (data.max() != null) {
                builder.setMax(data.max());
            }

            return builder.build().toByteArray();
        } else {
            return null;
        }
    }

    private AlertData.Severity mapSeverity(Alert.Severity severity) {
        return switch (severity) {
            case FAULT -> AlertData.Severity.FAULT;
            case WARNING -> AlertData.Severity.WARNING;
            case CRITICAL -> AlertData.Severity.CRITICAL;
        };
    }
}