package dev.nez.simulation;

import dev.nez.simulation.client.ProducerClient;
import dev.nez.simulation.dto.mqtt.AirQuality;
import dev.nez.simulation.dto.mqtt.Battery;
import dev.nez.simulation.model.Device;
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

    Integer numDevices = 5000;

    void onStart(@Observes StartupEvent ev) {
        for (int i = 0; i < numDevices; i++) {
            var dev2 = new Device("hardware" + (i + 3), "pass", "dev/air/p", "dev/batt/pow");

            producerClient.startSimulation(
                    new DeviceDataProducer(
                            dev2,
                            DeviceDataProducer.MessageType.PROTO,
                            new MessageTiming(TimeUnit.MILLISECONDS,0L, 1000L, 5),
                            new MessageTiming(TimeUnit.MILLISECONDS,0L, 5000L, 5)
                    ) {
                        @Override
                        public Object getData() {
                            return new AirQuality(
                                    this.deviceId,
                                    350 + random.nextInt(600),
                                    random.nextFloat() * 15.0f,
                                    random.nextFloat() * 30.0f,
                                    random.nextFloat() * 0.5f,
                                    25.0f + (random.nextFloat() * 5.0f),
                                    45.0f + (random.nextFloat() * 20.0f)
                            );
                        }

                        @Override
                        public Object getBatteryData() {
                            return new Battery(this.deviceId, 99.0f);
                        }
                    }
            );
        }
        var dev1 = new Device("hardware1", "pass", "dev/air/j");
        var dev2 = new Device("hardware2", "pass", "dev/air/p", "dev/batt/pow");

        producerClient.startSimulation(
            new DeviceDataProducer(
                    dev1,
                    DeviceDataProducer.MessageType.JSON,
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 5)
            ) {
                @Override
                public Object getData() {
                    return new AirQuality(
                        this.deviceId,
                        400 + random.nextInt(600),
                        random.nextFloat() * 15.0f,
                        random.nextFloat() * 30.0f,
                        random.nextFloat() * 0.5f,
                        20.0f + (random.nextFloat() * 5.0f),
                        40.0f + (random.nextFloat() * 20.0f)
                    );
                }
            }
        );

        producerClient.startSimulation(
            new DeviceDataProducer(
                    dev2,
                    DeviceDataProducer.MessageType.PROTO,
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 2000L, 5),
                    new MessageTiming(TimeUnit.MILLISECONDS,0L, 5000L, 5)
            ) {
                @Override
                public Object getData() {
                    return new AirQuality(
                        this.deviceId,
                        350 + random.nextInt(600),
                        random.nextFloat() * 15.0f,
                        random.nextFloat() * 30.0f,
                        random.nextFloat() * 0.5f,
                        25.0f + (random.nextFloat() * 5.0f),
                        45.0f + (random.nextFloat() * 20.0f)
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