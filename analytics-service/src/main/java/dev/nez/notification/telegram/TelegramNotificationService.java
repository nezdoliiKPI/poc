package dev.nez.notification.telegram;

import dev.nez.notification.Alert;
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
    private final int MAX_MSG_LENGTH = 2000;

    private final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM HH:mm:ss").withZone(ZoneId.systemDefault());

    @ConfigProperty(name = "telegram.bot.token")
    String botToken;

    @ConfigProperty(name = "telegram.channel.id")
    String channelId;

    @ConfigProperty(name = "telegram.client.use")
    Boolean useTelegram;

    @Inject
    @RestClient
    TelegramRestClient telegramRestClient;

    void onStart(@Observes StartupEvent ev) {
        if (!useTelegram) {
            Log.info("TelegramNotification bean is disabled via config. Alert sending from this bean is suspended.");
        }
    }

    @Incoming("telegram-in")
    Uni<Void> handle(List<Alert> batch) {
        if (!useTelegram || batch.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return sendToTelegram(
            buildFinalMessage(
                groupAndDeduplicateAlerts(batch)));
    }

    private Map<Long, Map<String, Instant>> groupAndDeduplicateAlerts(List<Alert> batch) {
        return batch.stream()
            .collect(Collectors.groupingBy(
                Alert::id,
                Collectors.flatMapping(
                    alert -> alert.msg().stream().map(msg -> Map.entry(msg, alert.ts())),
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (ts1, ts2) -> ts1.isAfter(ts2) ? ts1 : ts2
                    )
                )
            ));
    }

    private String buildFinalMessage(Map<Long, Map<String, Instant>> groupedAlerts) {
        String detailedMessage = buildDetailedMessage(groupedAlerts);

        return detailedMessage.length() <= MAX_MSG_LENGTH
            ? detailedMessage
            : buildCompactMessage(groupedAlerts);
    }

    private String buildDetailedMessage(Map<Long, Map<String, Instant>> groupedAlerts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Alerts</b>\n\n");

        for (Map.Entry<Long, Map<String, Instant>> entry : groupedAlerts.entrySet()) {
            sb.append("<b>Dev ID: ").append(entry.getKey()).append("</b>\n");

            entry.getValue().forEach(
                (msg, ts) -> sb
                        .append("  • [")
                        .append(TIME_FORMATTER.format(ts))
                        .append("] ")
                        .append(msg)
                        .append("\n")
                );

            sb.append("\n");
        }

        return sb.toString().trim();
    }

    private String buildCompactMessage(Map<Long, Map<String, Instant>> groupedAlerts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Alerts</b>\n\n");
        sb.append("Too many alerts to display, Device IDs:\n\n");

        String idsList = groupedAlerts.keySet().stream()
            .sorted()
            .map(Object::toString)
            .collect(Collectors.joining(", "));

        sb.append(idsList);

        if (sb.length() > MAX_MSG_LENGTH) {
            return sb.substring(0, MAX_MSG_LENGTH - 3) + "...";
        }

        return sb.toString();
    }

    private Uni<Void> sendToTelegram(String text) {
        final var message = new TelegramMessage(channelId, text, "HTML");

        return telegramRestClient.sendMessage(botToken, message)
            .onFailure().invoke(err -> Log.warnf("Telegram error: %s", err.getMessage()))
            .onFailure().retry().withBackOff(Duration.ofSeconds(2), Duration.ofSeconds(10)).atMost(3)
            .onItem().delayIt().by(Duration.ofSeconds(10))
            .replaceWithVoid();
    }
}