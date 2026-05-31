package dev.nez.monitoring.service;

import dev.nez.monitoring.dto.*;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class HistoryService {

    @Inject
    Pool db;

    public Uni<List<PowerConsumptionPoint>> getPowerHistory(
        long deviceId, OffsetDateTime from, OffsetDateTime to) {

        final String sql = """
            SELECT MAX(time_date) AS bucket,
                   device_id,
                   AVG(voltage) AS voltage,
                   AVG(current) AS current,
                   AVG(power)   AS power
            FROM power_consumption
            WHERE device_id = $1
              AND time_date BETWEEN $2 AND $3
            GROUP BY time_bucket('%s', time_date), device_id
            ORDER BY bucket
            """.formatted(resolveBucket(from, to));

        return db.preparedQuery(sql)
            .execute(Tuple.of(deviceId, from, to))
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
        long deviceId, OffsetDateTime from, OffsetDateTime to) {

        final String sql = """
            SELECT MAX(time_date) AS bucket,
                   device_id,
                   AVG(temperature) AS temperature,
                   AVG(humidity)    AS humidity
            FROM temperature_data
            WHERE device_id = $1
              AND time_date BETWEEN $2 AND $3
            GROUP BY time_bucket('%s', time_date), device_id
            ORDER BY bucket
            """.formatted(resolveBucket(from, to));

        return db.preparedQuery(sql)
            .execute(Tuple.of(deviceId, from, to))
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
        long deviceId, OffsetDateTime from, OffsetDateTime to) {

        final String sql = """
            SELECT MAX(time_date) AS bucket,
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
            GROUP BY time_bucket('%s', time_date), device_id
            ORDER BY bucket
            """.formatted(resolveBucket(from, to));

        return db.preparedQuery(sql)
            .execute(Tuple.of(deviceId, from, to))
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
        long deviceId, OffsetDateTime from, OffsetDateTime to) {

        final String sql = """
            SELECT MAX(time_date) AS bucket,
                   device_id,
                   AVG(val) AS val
            FROM battery_data
            WHERE device_id = $1
              AND time_date BETWEEN $2 AND $3
            GROUP BY time_bucket('%s', time_date), device_id
            ORDER BY bucket
            """.formatted(resolveBucket(from, to));

        return db.preparedQuery(sql)
            .execute(Tuple.of(deviceId, from, to))
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
        long deviceId, OffsetDateTime from, OffsetDateTime to) {

        final String sql = """
            SELECT MAX(time_date) AS bucket,
                   device_id,
                   AVG(smoke_raw) AS smoke_raw,
                   AVG(co_level)  AS co_level
            FROM smoke_detector
            WHERE device_id = $1
              AND time_date BETWEEN $2 AND $3
            GROUP BY time_bucket('%s', time_date), device_id
            ORDER BY bucket
            """.formatted(resolveBucket(from, to));

        return db.preparedQuery(sql)
            .execute(Tuple.of(deviceId, from, to))
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

    public Uni<List<Alert>> getAlertHistory(
        List<Long> deviceIds,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        final String sql = """
            SELECT alert_uuid,
                   device_id,
                   metric,
                   value,
                   min_threshold,
                   max_threshold,
                   severity,
                   message,
                   time_date
            FROM alerts
            WHERE device_id = ANY($1)
              AND time_date BETWEEN $2 AND $3
            ORDER BY time_date DESC
        """;

        final Long[] idsArray = deviceIds.toArray(new Long[0]);

        return db.preparedQuery(sql)
            .execute(Tuple.of(idsArray, from, to))
            .map(rows -> rows.stream()
                .map(row -> new Alert(
                    row.getUUID("alert_uuid"),
                    row.getLong("device_id"),
                    row.getString("metric"),
                    row.getFloat("value"),
                    row.getFloat("min_threshold"),
                    row.getFloat("max_threshold"),
                    Alert.Severity.valueOf(row.getString("severity")),
                    row.getString("message"),
                    row.getOffsetDateTime("time_date").toInstant()
                ))
                .toList()
            );
    }

    private String resolveBucket(OffsetDateTime from, OffsetDateTime to) {
        long seconds = Math.abs(ChronoUnit.SECONDS.between(from, to));

        if (seconds <= 300) return "1 seconds";
        if (seconds <= 1800) return "5 seconds";
        if (seconds <= 3600) return "30 seconds";
        if (seconds <= 86400) return "5 minutes";
        if (seconds <= 604800) return "1 hour";
        return "1 day";
    }
}