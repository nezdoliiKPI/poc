package dev.nez.auth;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class AdminAuthProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @ConfigProperty(name = "ADMIN_USERNAME")
    String username;

    @ConfigProperty(name = "ADMIN_PASSWORD")
    String pass;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(
        UsernamePasswordAuthenticationRequest request,
      AuthenticationRequestContext context
    ) {
        String username = request.getUsername();
        String password = new String(request.getPassword().getPassword());

        if (this.username.equals(username) && pass.equals(password)) {
            SecurityIdentity identity = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(username))
                .addRole("admin")
                .build();

            return Uni.createFrom().item(identity);
        }

        return Uni.createFrom().failure(new AuthenticationFailedException("Invalid credentials"));
    }
}
