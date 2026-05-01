package dev.nez.consumer.consumer;

import dev.nez.consumer.entity.Timed;
import dev.nez.consumer.metrics.MetricsRecorder;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BaseBatchConsumer<T extends Timed> {

    private static final Logger log = LoggerFactory.getLogger(BaseBatchConsumer.class);
    @Inject
    protected MetricsRecorder recorder;

    @Inject
    protected Pool sqlClient;

    protected Uni<Void> consumeBatch(
        List<T> batch,
        @Language("SQL") String sql,
        String channel,
        Function<T, Tuple> mapper
    ) {
        var tuples = new ArrayList<Tuple>(batch.size());
        for (var data : batch) tuples.add(mapper.apply(data));

        return sqlClient.withTransaction(conn -> conn.preparedQuery(sql).executeBatch(tuples))
            .replaceWithVoid()
            .onFailure()
            .invoke(throwable -> Log.error("Consume messages error from topic " + channel, throwable))
            .eventually(() -> {
                var now = Instant.now();
                for (var data : batch) recorder.recordMessageDelay(channel, data.timestamp().until(now));
            });
    }
}
