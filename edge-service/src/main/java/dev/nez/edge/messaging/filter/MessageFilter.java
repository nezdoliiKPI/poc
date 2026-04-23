package dev.nez.edge.messaging.filter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MessageFilter {

    @Inject
    DynamicFilterConfig dynamicFilterConfig;

    public class TopicFilter {

    }
}
