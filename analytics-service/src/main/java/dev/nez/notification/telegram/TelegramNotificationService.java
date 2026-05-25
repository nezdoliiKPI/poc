package dev.nez.notification.telegram;

import dev.nez.notification.Alert;
import dev.nez.notification.AlertDeserializer;
import dev.nez.notification.telegram.TelegramRestClient.TelegramMessage;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelegramNotificationService {
    private final DateTimeFormatter TIME_FORMATTER;
    private final AlertDeserializer deserializer = new AlertDeserializer();

    @ConfigProperty(name = "telegram.bot.token")
    String botToken;

    @ConfigProperty(name = "telegram.channel.id")
    String channelId;

    @ConfigProperty(name = "telegram.client.use")
    Boolean useTelegram;

    @Inject
    @RestClient
    TelegramRestClient telegramRestClient;

    public TelegramNotificationService() {
        TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.of("Europe/Berlin"));
    }

    void onStart(@Observes StartupEvent ev) {
        if (!useTelegram) {
            Log.info("TelegramNotification bean is disabled via config. Alert sending from this bean is suspended.");
        }
    }

    @Incoming("telegram-in")
    Uni<Void> handle(List<byte[]> batch) {
        if (!useTelegram || batch.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        List<Alert> data;

        try {
            data = batch.stream()
                .map(alert -> deserializer.deserialize("telegram-in", alert))
                .toList();
        } catch (RuntimeException e) {
            Log.error(e.getMessage());
            return Uni.createFrom().voidItem();
        }

        if (data.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return sendToTelegram(buildFinalMessage(groupAndDeduplicateAlerts(data)));
    }

    private Map<Long, Map<String, Instant>> groupAndDeduplicateAlerts(List<Alert> batch) {
        return batch.stream()
            .collect(Collectors.groupingBy(
                Alert::id,
                Collectors.toMap(
                    Alert::msg,
                    Alert::ts,
                    (ts1, ts2) -> ts1.isAfter(ts2) ? ts1 : ts2
                )
            ));
    }

    private String buildFinalMessage(Map<Long, Map<String, Instant>> groupedAlerts) {
        final int MAX_MSG_LENGTH = 500;
        final var mainBuilder = new StringBuilder().append("<b>Alerts</b>\n");

        for (Map.Entry<Long, Map<String, Instant>> entry : groupedAlerts.entrySet()) {
            final var devBuilder = new StringBuilder()
                .append("<b>Dev ID: ")
                .append(entry.getKey())
                .append("</b>\n");

            for (Map.Entry<String, Instant> alertEntry : entry.getValue().entrySet()) {
                devBuilder
                    .append("  • [")
                    .append(TIME_FORMATTER.format(alertEntry.getValue()))
                    .append("] ")
                    .append(alertEntry.getKey())
                    .append("\n");
            }

            if (mainBuilder.length() + devBuilder.length() <= MAX_MSG_LENGTH) {
                mainBuilder.append("\n");
                mainBuilder.append(devBuilder);
            } else  {
                mainBuilder
                    .append("\nMessage is too large. Total devices with alerts: ")
                    .append(groupedAlerts.size());
                break;
            }
        }

        return mainBuilder.toString().trim();
    }

    private Uni<Void> sendToTelegram(String text) {
        final var message = new TelegramMessage(channelId, text, "HTML");

        return telegramRestClient.sendMessage(botToken, message)
            .onFailure().invoke(err -> Log.warnf("Telegram error: %s", err.getMessage()))
            .onFailure().recoverWithNull()
            .onItem().delayIt().by(Duration.ofSeconds(10))
            .replaceWithVoid();
    }
}