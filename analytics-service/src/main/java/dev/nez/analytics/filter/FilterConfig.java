package dev.nez.analytics.filter;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithParentName;

import java.util.Map;

@ConfigMapping(prefix = "filter")
public interface FilterConfig {

    Delay delay();

    @WithParentName
    Map<String, TopicConfig> topics();

    interface Delay {
        @WithDefault("5")
        Integer min();
    }

    interface TopicConfig {
        @WithDefault("0")
        Integer threshold();
    }
}
