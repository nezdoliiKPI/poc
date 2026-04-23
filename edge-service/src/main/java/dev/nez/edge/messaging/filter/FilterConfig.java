package dev.nez.edge.messaging.filter;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "gen")
public interface FilterConfig {
    ChannelConfig powerIn();
    ChannelConfig smokeIn();
    ChannelConfig airIn();
    ChannelConfig battIn();

    interface ChannelConfig {
        @WithDefault("true")
        Boolean consume();

        @WithDefault("true")
        Boolean optimise();

        @WithDefault("0")
        Integer threshold();
    }
}
