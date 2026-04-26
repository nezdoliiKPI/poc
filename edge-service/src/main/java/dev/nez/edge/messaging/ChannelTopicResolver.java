package dev.nez.edge.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ChannelTopicResolver {
    private final Map<String, String> channelToTopic = new HashMap<>();

    public ChannelTopicResolver(Config config) {
        StreamSupport.stream(config.getPropertyNames().spliterator(), false)
            .filter(propertyName ->
                        propertyName.startsWith("mp.messaging.incoming.") &&
                            propertyName.endsWith(".topic"))
            .forEach(propertyName -> {
                final String channelName = propertyName
                    .replace("mp.messaging.incoming.", "")
                    .replace(".topic", "");

                String topicName = config.getValue(propertyName, String.class);
                channelToTopic.put(channelName, topicName);
            });
    }

    public Optional<String> getTopic(String channelName) {
        return Optional.ofNullable(channelToTopic.get(channelName));
    }

    public Map<String, String> getAllTopics() {
        return Map.copyOf(channelToTopic);
    }
}
