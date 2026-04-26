package dev.nez.edge.messaging.filter;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithParentName;

import java.util.Map;

@ConfigMapping(prefix = "filter")
public interface FilterConfig {

    @WithParentName
    Map<String, TopicConfig> channels();

    interface TopicConfig {
        @WithDefault("true")
        Boolean consume();

        @WithDefault("0")
        Integer threshold();
    }
}
