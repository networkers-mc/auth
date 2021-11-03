package es.networkersmc.authplugin.session;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import es.networkersmc.authplugin.data.AuthenticationDataDAO;
import es.networkersmc.authplugin.data.AuthenticationDataRepository;
import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.authplugin.security.EncryptionService;
import es.networkersmc.dendera.bukkit.network.NetworkService;
import es.networkersmc.dendera.docs.User;
import es.networkersmc.dendera.network.bukkit.BukkitNodeType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Singleton
public class AuthSessionService implements Listener {

    private final Map<UUID, AuthSession> sessions = Maps.newConcurrentMap();

    @Inject private ExecutorService asyncExecutor;
    @Inject private NetworkService networkService;

    @Inject private EncryptionService encryptionService;
    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private AuthenticationDataDAO dataDAO;

    public AuthSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    // Info: Called asynchronously from UserPreLoginEvent
    // Don't worry about sync calls to database
    public void load(User user) {
        UUID uuid = user.getUUID();
        AuthSession session = dataRepository.getSync(uuid)
                .map(data -> new AuthSession(user, AuthState.LOGIN, data))
                .orElseGet(() -> new AuthSession(user, AuthState.REGISTER, dataDAO.create(uuid)));

        sessions.put(uuid, session);
    }

    public void unload(UUID uuid) {
        sessions.remove(uuid);
    }

    /**
     * Checks if the given password is valid to log in.
     *
     * @return {@link ListenableFuture<Void>} that will throw {@link IllegalArgumentException} if the password isn't valid.
     */
    public ListenableFuture<Void> loginAsync(AuthSession session, String input) {
        SettableFuture<Void> future = SettableFuture.create();
        async(() -> {
            if (this.loginSync(session, input)) {
                future.set(null);
            } else {
                future.setException(new IllegalArgumentException("Invalid password"));
            }
        });
        return future;
    }

    /**
     * Checks if the given password is valid to log in.
     *
     * @return {@code true} if the password is valid, {@code false} otherwise.
     */
    public boolean loginSync(AuthSession session, String input) {
        return encryptionService.verify(input, session.getData().getPasswordHash());
    }

    public ListenableFuture<Void> registerAsync(AuthSession session, char[] hash) {
        SettableFuture<Void> future = SettableFuture.create();
        async(() -> {
            this.registerSync(session, hash);
            future.set(null);
        });
        return future;
    }

    public void registerSync(AuthSession session, char[] hash) {
        AuthenticationData data = session.getData();
        data.setPasswordHash(new String(hash));
        dataRepository.updateSync(data);
    }

    public void forceLogin(Player player) {
        networkService.sendToBestNode(player, BukkitNodeType.HUB);
    }

    private void async(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

}
