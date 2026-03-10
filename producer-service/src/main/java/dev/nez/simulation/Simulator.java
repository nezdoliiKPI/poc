package dev.nez.simulation;

import dev.nez.simulation.client.ProducerClient;
import dev.nez.simulation.dto.mqtt.Battery;
import dev.nez.simulation.model.Device;
import dev.nez.simulation.dto.mqtt.TemperatureHumidity;
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

        var dev1 = new Device("hardware1", "pass", "dev/tempH/j");
        var dev2 = new Device("hardware2", "pass", "dev/tempH/pow", "dev/batt/pow");

        producerClient.startSimulation(
            new DeviceDataProducer(
                    dev1,
                    DeviceDataProducer.MessageType.JSON,
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 100000)
            ) {
                @Override
                public Object getData() {
                    return new TemperatureHumidity(
                        this.deviceId,
                        20.0f + random.nextFloat() * 10.0f,
                        50.0f + random.nextFloat() * 50.0f
                    );
                }
            }
        );

        producerClient.startSimulation(
            new DeviceDataProducer(
                    dev2,
                    DeviceDataProducer.MessageType.PROTO,
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 100000),
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 5000L, 100000)
            ) {
                @Override
                public Object getData() {
                    return new TemperatureHumidity(
                        this.deviceId,
                        40.0f + random.nextFloat() * 10.0f,
                        40.0f + random.nextFloat() * 60.0f
                    );
                }

                @Override
                public Object getBatteryData() {
                    return new Battery(this.deviceId, 99.0f);
                }
            }
        );
    }
}