package dev.nez.simulation;

import dev.nez.simulation.client.ProducerClient;
import dev.nez.simulation.model.Device;
import dev.nez.simulation.dto.mqtt.Temperature;
import dev.nez.simulation.model.MessageTiming;
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

        var dev1 = new Device("hardware1", "pass", "dev/temp/j");
        var dev2 = new Device("hardware2", "pass", "dev/temp/p");

        producerClient.startSimulation(
            new DeviceDataProducer(
                dev1,
                DeviceDataProducer.MESSAGE_TYPE.JSON,
                new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 100000)
            ) {
                @Override
                public Object getData() {
                    return new Temperature(this.deviceId, 20.0 + random.nextDouble() * 10.0);
                }
            }
        );

        producerClient.startSimulation(
            new DeviceDataProducer(
                dev2,
                DeviceDataProducer.MESSAGE_TYPE.PROTO,
                new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 100000)
            ) {
                @Override
                public Object getData() {
                    return new Temperature(this.deviceId, 40.0 + random.nextDouble() * 10.0);
                }
            }
        );
    }
}