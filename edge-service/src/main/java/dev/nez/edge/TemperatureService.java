package dev.nez.edge;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.nez.edge.dto.mqtt.Temperature;
import dev.nez.edge.dto.mqtt.TemperatureMessage;
import io.quarkus.logging.Log;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class TemperatureService {

    @Incoming("temp-out")
    @Merge
    public  Uni<Void> trash(Temperature telemetry) {
        return  Uni.createFrom().nullItem();
    }

    @Incoming("temp-j-in")
    @Outgoing("temp-out")
    public Multi<Message<Temperature>> consumeTemperatureJson(Multi<Message<byte[]>> stream) {
        return stream
            .onItem().transformToMultiAndConcatenate(msg -> transformJsonMessage(msg, Temperature.class))
            .group().by(msg -> msg.getPayload().deviceId())
            .onItem().transformToMultiAndMerge(deviceStream -> deviceStream
                .group().intoLists().of(5, Duration.ofSeconds(5))
                    .map(batch -> Message.of(processBatch(batch) , () -> {
                        var size = batch.size();
                        var futures = new CompletableFuture[size];

                        for (int i = 0; i < size; i++) {
                            futures[i] = batch.get(i).ack().toCompletableFuture();
                        }
                        return CompletableFuture.allOf(futures);
                    }))
            );
    }

    @Incoming("temp-p-in")
    @Outgoing("temp-out")
    public Uni<Temperature> consumeTemperatureProto(byte[] payload) {
        return Uni.createFrom().item(() -> payload)
                .map(p -> {
                    try {
                        return TemperatureMessage.parseFrom(p);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(msg -> new Temperature(msg.getDeviceId(), msg.getTemp()))
                .onItem().invoke(telemetry -> Log.info("Received from proto: " + telemetry))
                .onFailure().invoke(e -> Log.error(e.getMessage()))
                .onFailure().recoverWithNull();
    }

    private <T> Multi<Message<T>> transformJsonMessage(Message<byte[]> msg, Class<T> clazz) {
        try {
            var buffer = Buffer.buffer(msg.getPayload());
            T data = Json.decodeValue(buffer.getDelegate(), clazz);

            return Multi.createFrom().item(msg.withPayload(data));
        } catch (DecodeException e) {
            Log.error(e.getMessage()); //TODO Log.error
            msg.nack(e);

            return Multi.createFrom().empty();
        }
    }

    private Temperature processBatch(List<Message<Temperature>> batch) {
        for (var msg : batch) {
            Log.info("Processed: " + msg.getPayload()); //TODO process
        }

        return batch.getLast().getPayload();
    }
}