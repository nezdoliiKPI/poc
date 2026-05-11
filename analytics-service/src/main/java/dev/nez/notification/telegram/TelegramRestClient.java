package dev.nez.notification.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "telegram-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TelegramRestClient {

    @POST
    @Path("/bot{token}/sendMessage")
    Uni<Response> sendMessage(@PathParam("token") String token, TelegramMessage request);

    record TelegramMessage(
        @JsonProperty("chat_id") String channelId,
        @JsonProperty("text") String messageText,
        @JsonProperty("parse_mode") String parseMode
    ) {}
}
