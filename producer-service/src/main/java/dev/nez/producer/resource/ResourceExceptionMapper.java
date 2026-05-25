package dev.nez.producer.resource;

import io.smallrye.faulttolerance.api.RateLimitException;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ResourceExceptionMapper {
    @ServerExceptionMapper
    public RestResponse<String> mapBulkheadException(BulkheadException ex) {
        return RestResponse.status(
            RestResponse.Status.TOO_MANY_REQUESTS,
            "Your request is currently being processed. Please try again later."
        );
    }

    @ServerExceptionMapper
    public RestResponse<String> handleRateLimit(RateLimitException ex) {
        return RestResponse.status(
            RestResponse.Status.TOO_MANY_REQUESTS,
            "Too Many Requests: Please slow down."
        );
    }
}
