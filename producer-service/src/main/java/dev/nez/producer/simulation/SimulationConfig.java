package dev.nez.producer.simulation;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "gen")
public interface SimulationConfig {
    DeviceConfig air();
    DeviceConfig power();
    DeviceConfig smoke();
    DeviceConfig battery();

    interface DeviceConfig {
        ProtocolConfig proto();
        ProtocolConfig json();
    }

    interface ProtocolConfig {
        @WithDefault("0")
        Integer count();
        String topic();
    }
}
