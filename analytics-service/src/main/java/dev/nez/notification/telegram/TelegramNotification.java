package dev.nez.notification.telegram;

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
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelegramNotification {

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
    Uni<Void> handle(List<String> batch) {
        if (!useTelegram ||batch.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        String combinedMessage = batch.stream()
            .map(msg -> "• " + msg)
            .collect(Collectors.joining("\n"));

        if (combinedMessage.length() > 4000) {
            combinedMessage = combinedMessage.substring(0, 3997) + "...";
        }

        final var message = new TelegramRestClient.TelegramMessage(channelId, combinedMessage, "HTML");

        return telegramRestClient.sendMessage(botToken, message)
            .onFailure().invoke(err -> Log.warnf("Telegram error: %s", err.getMessage()))
            .onFailure().retry().withBackOff(Duration.ofSeconds(2), Duration.ofSeconds(10)).atMost(2)
            .replaceWithVoid()
            .onFailure().recoverWithUni(Uni.createFrom().voidItem())
            .onItem().delayIt().by(Duration.ofSeconds(10));
    }
}