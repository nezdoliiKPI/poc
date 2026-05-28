package dev.nez.analytics.filter;

import dev.nez.analytics.data.alert.Alert;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

@Singleton
public class NotificationFilter {

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    FilterConfig defaultConfig;

    public FilterConfig getConfig() {
        return defaultConfig;
    }

    public TopicFilter newTopicFilter(BiFunction<Alert, Alert, Boolean> filterCondition) {
        final var topicConfig = Objects.requireNonNull(
            defaultConfig.topics().get(notificationsTopic),
            "No such config for topic: " + notificationsTopic
        );

        return new TopicFilter(filterCondition, topicConfig);
    }

    public static class TopicFilter {
        private final ConcurrentMap<FilterKey, Value> lastValues = new ConcurrentHashMap<>();

        private final BiFunction<Alert, Alert, Boolean> filterCondition;
        private final FilterConfig.TopicConfig config;

        TopicFilter(
            BiFunction<Alert, Alert, Boolean> filterCondition,
            FilterConfig.TopicConfig config
        ) {
            this.filterCondition = filterCondition;
            this.config = config;
        }

        public boolean apply(Alert alert) {
            final var conf = config;

            if (conf.threshold() == 0) {
                return true;
            }

            final var key = new FilterKey(alert.dID(), alert.metric());
            final var lastData = lastValues.get(key);

            if (lastData == null) {
                lastValues.put(key, new Value(alert));
                return true;
            }

            if (filterCondition.apply(lastData.data, alert)) {
                lastData.count = 0;
                lastData.data = alert;
                return true;
            }

            if (conf.threshold() > lastData.count) {
                lastData.count++;
                return false;
            }

            lastData.count = 0;
            lastData.data = alert;
            return true;
        }

        private static class Value {
            public int count = 0;
            public Alert data;

            private Value(Alert data) {
                this.data = data;
            }
        }

        private record FilterKey(
            Long deviceId,
            String metric) {
        }
    }
}