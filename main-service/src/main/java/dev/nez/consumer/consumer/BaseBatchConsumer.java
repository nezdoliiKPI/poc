package dev.nez.consumer.consumer;

import com.google.protobuf.Timestamp;
import dev.nez.consumer.metrics.MetricsRecorder;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;

import org.intellij.lang.annotations.Language;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BaseBatchConsumer<T> {
    private final String channel;
    private final Function<T, Tuple> mapper;
    private final Function<T, Timestamp> getTimestamp;
    private final String sql;

    @Inject
    Pool sqlClient;

    @Inject
    MetricsRecorder recorder;

    BaseBatchConsumer(
        String channel,
        Function<T, Tuple> mapper,
        Function<T, Timestamp> getTimestamp,
        @Language("SQL") String sql
    ) {
        this.channel = channel;
        this.mapper = mapper;
        this.getTimestamp = getTimestamp;
        this.sql = sql;
    }

    protected Uni<Void> consumeBatch(Message<List<T>> batchMessage) {
        final var batch = batchMessage.getPayload();
        final var tuples = new ArrayList<Tuple>(batch.size());
        for (var data : batch) tuples.add(mapper.apply(data));

        return sqlClient.withTransaction(conn -> conn.preparedQuery(sql).executeBatch(tuples))
            .call(() -> Uni.createFrom().completionStage(batchMessage.ack()))
            .onFailure().call(throwable -> {
                Log.error("Consume messages error from topic " + channel, throwable);
                return Uni.createFrom().completionStage(batchMessage.nack(throwable));
            })
            .eventually(() -> {
                final var now = Instant.now();

                for (var data : batch) {
                    final var timestamp = getTimestamp.apply(data);
                    final var time = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
                    recorder.recordMessageDelay(channel, time.until(now));
                }
            })
            .replaceWithVoid();
    }
}
