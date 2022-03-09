package es.networkersmc.auth.session;

import com.google.common.collect.Maps;

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AuthSessionCache {

    private final Map<UUID, AuthSession> sessions = Maps.newConcurrentMap();

    public void cache(UUID uuid, AuthSession session) {
        sessions.put(uuid, session);
    }

    public AuthSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public void remove(UUID uuid) {
        sessions.remove(uuid);
    }
}
