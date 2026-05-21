package dev.nez.analytics.topology.stream;

import dev.nez.analytics.filter.NotificationFilter;
import dev.nez.notification.Alert;
import org.apache.kafka.streams.StreamsBuilder;

import java.time.Duration;
import java.util.function.BiFunction;

public abstract class TelemetryStreamBase {
    protected final NotificationFilter.TopicFilter<Alert> filter;

    protected TelemetryStreamBase(NotificationFilter notificationFilter) {
        BiFunction<Alert, Alert, Boolean> alertFilter = (oldData, newData) -> {
            final Duration diff = Duration.between(oldData.ts(), newData.ts());
            return diff.toMinutes() >= notificationFilter.getConfig().delay().min() || !oldData.msg().equals(newData.msg());
        };

        this.filter = notificationFilter.newTopicFilter(alertFilter);
    }

    public abstract void addTopology(StreamsBuilder builder);

}
