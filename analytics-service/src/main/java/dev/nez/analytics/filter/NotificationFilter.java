package dev.nez.analytics.filter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

@Singleton
public class NotificationFilter {
    private final ConcurrentMap<String, TopicFilter<?>> filters = new ConcurrentHashMap<>();

    @ConfigProperty(name = "kafka.notifications.topic")
    String notificationsTopic;

    @Inject
    FilterConfig defaultConfig;

    public FilterConfig getConfig() {
        return defaultConfig;
    }

    public <T> TopicFilter<T> newTopicFilter(BiFunction<T, T, Boolean> filter) {
        final var topicConfig = Objects.requireNonNull(
            defaultConfig.topics().get(notificationsTopic), "No such config for topic: " + notificationsTopic
        );

        return new TopicFilter<>(filter, topicConfig);
    }

    public static class TopicFilter<T> {
        private final ConcurrentMap<Long, Value> lastValues = new ConcurrentHashMap<>();
        private final BiFunction<T, T, Boolean> filter;
        private final FilterConfig.TopicConfig config;

        TopicFilter(
            BiFunction<T, T, Boolean> filter,
            FilterConfig.TopicConfig config
        ) {
            this.filter = filter;
            this.config = config;
        }

        public boolean apply(long deviceId, T data) {
            final var conf = config;

            if (conf.threshold() == 0) {
                return true;
            }

            final var lastData = lastValues.get(deviceId);

            if (lastData == null) {
                lastValues.put(deviceId, new Value(data));
                return true;
            }
            if (filter.apply(lastData.data, data)) {
                lastData.count = 0;
                lastData.data = data;
                return true;
            }
            if (conf.threshold() > lastData.count) {
                lastData.count++;
                return false;
            }

            lastData.count = 0;
            lastData.data = data;
            return true;
        }

        private class Value {
            public int count = 0;
            public T data;

            private Value(T data) {
                this.data = data;
            }
        }
    }
}
