package dev.nez.consumer.consumer;

import dev.nez.consumer.entity.Timed;
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

public abstract class BaseBatchConsumer<T extends Timed> {
    private final String channel;

    @Inject
    Pool sqlClient;

    @Inject
    MetricsRecorder recorder;

    BaseBatchConsumer(String channel) {
        this.channel = channel;
    }

    protected Uni<Void> consumeBatch(
        Message<List<T>> batchMessage,
        @Language("SQL") String sql,
        Function<T, Tuple> mapper
    ) {
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
                for (var data : batch) recorder.recordMessageDelay(channel, data.timestamp().until(now));
            })
            .replaceWithVoid();
    }
}
