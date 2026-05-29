package dev.nez.auth;

import io.smallrye.common.constraint.Nullable;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionStore {
    private static final long SESSION_DURATION_MS = 30 * 60 * 1000L; // 30 minutes
    private final ConcurrentHashMap<String, Entry> sessions = new ConcurrentHashMap<>();

    private record Entry(
        String username,
        long expiresAt
    ) {}

    public String createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Entry(username, System.currentTimeMillis() + SESSION_DURATION_MS));
        return token;
    }

    @Nullable
    public String getUsername(String token) {
        Entry entry = sessions.get(token);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expiresAt()) {
            sessions.remove(token);
            return null;
        }
        return entry.username();
    }

    public void remove(String token) {
        if (token != null) sessions.remove(token);
    }
}