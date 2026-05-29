package dev.nez.auth;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.Optional;

@Alternative
@Priority(1)
@ApplicationScoped
public class CookieAuthMechanism implements HttpAuthenticationMechanism {
    static final String COOKIE_NAME = "session";

    @Inject
    SessionStore sessionStore;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        final var cookie = context.request().getCookie(COOKIE_NAME);

        if (cookie == null) {
            return Uni.createFrom().optional(Optional.empty());
        }

        final String username = sessionStore.getUsername(cookie.getValue());
        if (username == null) {
            return Uni.createFrom().optional(Optional.empty());
        }

        SecurityIdentity identity = QuarkusSecurityIdentity.builder()
            .setPrincipal(new QuarkusPrincipal(username))
            .addRole("admin")
            .build();

        return Uni.createFrom().item(identity);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }
}