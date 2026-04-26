package dev.nez.panel;

import dev.nez.edge.messaging.ChannelTopicResolver;
import dev.nez.edge.messaging.filter.MessageFilter;
import dev.nez.panel.dto.FilterConfigUpdate;
import io.smallrye.common.annotation.RunOnVirtualThread;

import io.vertx.core.eventbus.EventBus;

import jakarta.inject.Inject;

import jakarta.validation.Valid;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.AbstractMap.SimpleImmutableEntry;

@Path("/api/panel")
public class ConfigResource {

    @Inject
    ChannelTopicResolver channelTopicResolver;

    @Inject
    EventBus eventBus;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @Bulkhead(value = 1)
    public RestResponse<?> update(@Valid FilterConfigUpdate request) {
        if (!channelTopicResolver.getAllTopics().containsValue(request.topic())) {
            return RestResponse.status(Response.Status.BAD_REQUEST, "No such topic");
        }

        eventBus.send(MessageFilter.CONFIG_ADDRESS,
            new SimpleImmutableEntry<>(
                request.topic(),
                new MessageFilter.TopicConfig(request.consume(), request.threshold())
        ));

        return RestResponse.ok();
    }

    @ServerExceptionMapper
    public RestResponse<String> mapBulkheadException(BulkheadException ex) {
        return RestResponse.status(
            RestResponse.Status.TOO_MANY_REQUESTS,
            "Your request is currently being processed. Please try again later."
        );
    }
}