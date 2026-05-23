package dev.nez.service;

import dev.nez.model.*;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class HistoryService {

    @Inject
    PgPool db;

    private final String powerConsumptionSQL ="""
        SELECT time_bucket($4, time_date) AS bucket,
               device_id,
               AVG(voltage) AS voltage,
               AVG(current) AS current,
               AVG(power)   AS power
        FROM power_consumption
        WHERE device_id = $1
          AND time_date BETWEEN $2 AND $3
        GROUP BY bucket, device_id
        ORDER BY bucket
    """;

    private final String temperatureSQL ="""
        SELECT time_bucket($4, time_date) AS bucket,
               device_id,
               AVG(temperature) AS temperature,
               AVG(humidity)    AS humidity
        FROM temperature_data
        WHERE device_id = $1
          AND time_date BETWEEN $2 AND $3
        GROUP BY bucket, device_id
        ORDER BY bucket
    """;

    private final String airQualitySQL ="""
        SELECT time_bucket($4, time_date) AS bucket,
               device_id,
               AVG(co2)         AS co2,
               AVG(pm25)        AS pm25,
               AVG(pm10)        AS pm10,
               AVG(tvoc)        AS tvoc,
               AVG(temperature) AS temperature,
               AVG(humidity)    AS humidity
        FROM air_quality
        WHERE device_id = $1
          AND time_date BETWEEN $2 AND $3
        GROUP BY bucket, device_id
        ORDER BY bucket
    """;

    private final String battery="""
        SELECT time_bucket($4, time_date) AS bucket,
               device_id,
               AVG(val) AS val
        FROM battery_data
        WHERE device_id = $1
          AND time_date BETWEEN $2 AND $3
        GROUP BY bucket, device_id
        ORDER BY bucket
    """;

    private final String smokeDetectorSQL="""
        SELECT time_bucket($4, time_date) AS bucket,
               device_id,
               AVG(smoke_raw) AS smoke_raw,
               AVG(co_level)  AS co_level
        FROM smoke_detector
        WHERE device_id = $1
          AND time_date BETWEEN $2 AND $3
        GROUP BY bucket, device_id
        ORDER BY bucket
    """;

    public Uni<List<PowerConsumptionPoint>> getPowerHistory(
        long deviceId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String bucket = resolveBucket(from, to);

        return db.preparedQuery(powerConsumptionSQL)
            .execute(Tuple.of(deviceId, from, to, bucket))
            .map(rows -> rows.stream()
                .map(row -> new PowerConsumptionPoint(
                    row.getOffsetDateTime("bucket").toInstant(),
                    row.getLong("device_id"),
                    row.getFloat("voltage"),
                    row.getFloat("current"),
                    row.getFloat("power")
                ))
                .toList()
            );
    }

    public Uni<List<TemperaturePoint>> getTemperatureHistory(
        long deviceId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String bucket = resolveBucket(from, to);

        return db.preparedQuery(temperatureSQL)
            .execute(Tuple.of(deviceId, from, to, bucket))
            .map(rows -> rows.stream()
                .map(row -> new TemperaturePoint(
                    row.getOffsetDateTime("bucket").toInstant(),
                    row.getLong("device_id"),
                    row.getFloat("temperature"),
                    row.getFloat("humidity")
                ))
                .toList()
            );
    }

    public Uni<List<AirQualityPoint>> getAirQualityHistory(
        long deviceId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String bucket = resolveBucket(from, to);

        return db.preparedQuery(airQualitySQL)
            .execute(Tuple.of(deviceId, from, to, bucket))
            .map(rows -> rows.stream()
                .map(row -> new AirQualityPoint(
                    row.getOffsetDateTime("bucket").toInstant(),
                    row.getLong("device_id"),
                    row.getInteger("co2"),
                    row.getFloat("pm25"),
                    row.getFloat("pm10"),
                    row.getFloat("tvoc"),
                    row.getFloat("temperature"),
                    row.getFloat("humidity")
                ))
                .toList()
            );
    }

    public Uni<List<BatteryPoint>> getBatteryHistory(
        long deviceId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String bucket = resolveBucket(from, to);

        return db.preparedQuery(battery)
            .execute(Tuple.of(deviceId, from, to, bucket))
            .map(rows -> rows.stream()
                .map(row -> new BatteryPoint(
                    row.getOffsetDateTime("bucket").toInstant(),
                    row.getLong("device_id"),
                    row.getFloat("val")
                ))
                .toList()
            );
    }

    public Uni<List<SmokeDetectorPoint>> getSmokeHistory(
        long deviceId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String bucket = resolveBucket(from, to);

        return db.preparedQuery(smokeDetectorSQL)
            .execute(Tuple.of(deviceId, from, to, bucket))
            .map(rows -> rows.stream()
                .map(row -> new SmokeDetectorPoint(
                    row.getOffsetDateTime("bucket").toInstant(),
                    row.getLong("device_id"),
                    row.getInteger("smoke_raw"),
                    row.getInteger("co_level")
                ))
                .toList()
            );
    }

    private String resolveBucket(OffsetDateTime from, OffsetDateTime to) {
        final long hours = ChronoUnit.HOURS.between(from, to);

        if (hours <= 1)   return "1 minute";
        if (hours <= 24)  return "5 minutes";
        if (hours <= 168) return "1 hour";
        return "1 day";
    }
}