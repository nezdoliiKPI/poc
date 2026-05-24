package dev.nez.auth;

import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class BasicAuthPopupFilter {

    @SuppressWarnings("unused")
    public void init(@Observes Router router) {

        router.route().order(Integer.MIN_VALUE).handler(rc -> {
            rc.addHeadersEndHandler(v -> {
                if (rc.response().getStatusCode() == 401) {
                    rc.response().headers().remove("WWW-Authenticate");
                }
            });
            rc.next();
        });
    }
}
