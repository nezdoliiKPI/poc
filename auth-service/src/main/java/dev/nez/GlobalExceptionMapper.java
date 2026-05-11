package dev.nez;

import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class GlobalExceptionMapper {
    @ServerExceptionMapper
    public RestResponse<String> mapBulkheadException(BulkheadException ex) {
        return RestResponse.status(RestResponse.Status.TOO_MANY_REQUESTS);
    }
}
