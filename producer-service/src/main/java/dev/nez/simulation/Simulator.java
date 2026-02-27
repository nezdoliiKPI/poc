package dev.nez.simulation;

import dev.nez.simulation.client.ProducerClient;
import dev.nez.simulation.model.Device;
import dev.nez.simulation.model.Temperature;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class Simulator {

    @Inject
    ProducerClient producerClient;

    void onStart(@Observes StartupEvent ev) {
        Log.info("Start device simulation...");

        var dev = new Device("hardware1", "pass", "dev/temp/j");

        producerClient.startSimulation(
            new DeviceDataProducer(dev, 0L, 500L, TimeUnit.MILLISECONDS) {
                @Override
                public Object getData() {
                    return new Temperature(this.deviceId, 20.0 + random.nextDouble() * 10.0);
                }
            }
        );
    }
}