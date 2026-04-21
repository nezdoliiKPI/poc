package dev.nez.consumer.consumer;

import dev.nez.consumer.metrics.recorder.MetricsRecorder;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BaseBatchConsumer<T> {

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

        return sqlClient.withTransaction(
            conn -> conn.preparedQuery(sql).executeBatch(tuples)).replaceWithVoid()
            .eventually(() -> recorder.recordMessagesProcessed(channel, batch.size()));
    }
}
