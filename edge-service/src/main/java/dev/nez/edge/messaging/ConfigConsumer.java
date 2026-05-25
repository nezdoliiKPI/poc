package dev.nez.edge.messaging;

import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.edge.dto.rest.FilterConfig;
import io.quarkus.logging.Log;

import io.vertx.core.eventbus.EventBus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.AbstractMap.SimpleImmutableEntry;

@ApplicationScoped
public class ConfigConsumer {

    @Inject
    ChannelTopicResolver channelTopicResolver;

    @Inject
    EventBus eventBus;

    @Incoming("filter-config-in")
    public void consumeUpdate(FilterConfig request) {
        if (!channelTopicResolver.getAllTopics().containsValue(request.topic())) {
            Log.warnf("Received config update for unknown topic: %s. Ignoring.", request.topic());
            return;
        }

        eventBus.send(
            MessageFilter.CONFIG_ADDRESS,
            new SimpleImmutableEntry<>(
              request.topic(),
              new MessageFilter.TopicConfig(request.consume(), request.threshold())
        ));
    }
}