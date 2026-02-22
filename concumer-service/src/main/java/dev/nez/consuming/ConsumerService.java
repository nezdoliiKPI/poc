package dev.nez.consuming;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ConsumerService {

    @Incoming("telemetry-out")
    public  Uni<Void> trash(Temperature  telemetry) {
        return  Uni.createFrom().nullItem();
    }

    @Incoming("telemetry-in")
    @Outgoing("telemetry-out")
    public Multi<Message<Temperature>> processByDevice(Multi<Message<byte[]>> stream) {
        return stream
            .onItem().transformToMultiAndConcatenate(msg -> transformJsonMessage(msg, Temperature.class))
            .group().by(msg -> msg.getPayload().deviceId())
            .onItem().transformToMultiAndMerge(deviceStream -> deviceStream
                .group().intoLists().of(5, Duration.ofSeconds(5))
                    .onItem().transform(batch -> Message.of(processBatch(batch) , () -> {
                        var size = batch.size();
                        var futures = new CompletableFuture[size];

                        for (int i = 0; i < size; i++) {
                            futures[i] = batch.get(i).ack().toCompletableFuture();
                        }
                        return CompletableFuture.allOf(futures);
                    }))
            );
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

//    @Incoming("telemetry-in")
//    public Uni<Void> consumeTelemetry(byte[] payload) {
//        return Uni.createFrom().item(() -> Buffer.buffer(payload))
//                .onItem().transform(buffer -> Json.decodeValue(buffer.getDelegate(), Telemetry.class))
//                .onItem().invoke(telemetry -> Log.info("Received: " + telemetry))
//                .onFailure().invoke(e -> Log.error(e.getMessage()))
//                .onFailure().recoverWithNull()
//                .replaceWithVoid();
//    }
