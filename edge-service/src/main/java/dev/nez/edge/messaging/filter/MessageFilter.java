package dev.nez.edge.messaging.filter;

import dev.nez.edge.messaging.ChannelTopicResolver;
import dev.nez.edge.metrics.MetricsRecorder;
import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

@Singleton
public class MessageFilter {
    public static final String CONFIG_ADDRESS = "config-change-event";
    private final ConcurrentMap<String, ChannelFilter<?>> filters = new ConcurrentHashMap<>();

    @Inject
    FilterConfig defaultConfig;

    @Inject
    ChannelTopicResolver topicResolver;

    @Inject
    MetricsRecorder metricsRecorder;

    /**
     * Creates and registers a new {@link ChannelFilter} for the specified channel,
     * applying the provided comparison logic and fetching its specific dynamic configuration.
     *
     * @param filter  the function used to evaluate data (typically comparing the previous value
     * with the new one). Should return {@code true} if the data passes the filter.
     * @param channel the name of the messaging channel (e.g., "power-p-in").
     * @param <T>     the type of the payload data being filtered.
     * @return the previous {@link ChannelFilter} associated with the resolved topic,
     * or {@code null} if there was no prior filter for this topic.
     * @throws NullPointerException if the topic cannot be resolved for the given channel.
     */
    public <T> ChannelFilter<T> newChannelFilter(BiFunction<T, T, Boolean> filter, String channel) {
        final String topic = topicResolver.getTopic(channel)
            .orElseThrow(() -> new RuntimeException("topic not found"));

        final var topicConfig = Objects.requireNonNull(
            defaultConfig.channels().get(channel), "No such config for topic: " + topic
        );

        final var channelFilter = new ChannelFilter<>(topic, filter, topicConfig);
        if (this.filters.put(topic, channelFilter) != null) {
            throw new IllegalStateException("Duplicate channel filter: " + topic);
        }

        return channelFilter;
    }

    public record TopicConfig(
        Boolean consume,
        Integer threshold
    ) implements FilterConfig.TopicConfig {}

    @ConsumeEvent(CONFIG_ADDRESS)
    public void update(SimpleImmutableEntry<String, TopicConfig> event) {
        final var filter = Objects.requireNonNull(
            filters.get(event.getKey()), "No such filter for topic: " + event.getKey()
        );

        Log.info("Updated config " + event.getValue() + " for topic " + event.getKey());
        filter.setConfig(event.getValue());
    }

    public  class ChannelFilter<T> {
        private final String topic;
        private final ConcurrentMap<Long, Value> lastValues = new ConcurrentHashMap<>();
        private final BiFunction<T, T, Boolean> filter;

        private volatile FilterConfig.TopicConfig config;

        ChannelFilter(
            String topic,
            BiFunction<T, T, Boolean> filter,
            FilterConfig.TopicConfig config
        ) {
            this.topic = topic;
            this.filter = filter;
            this.config = config;
        }

        public void setConfig(FilterConfig.TopicConfig config) {
            this.config = config;
        }

        public boolean shouldConsume() {
            if (config.consume()) {
                return true;
            }

            metricsRecorder.recordMessageFilterDrop(topic);
            return false;
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
                metricsRecorder.recordMessageFilterDrop(topic);
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
