package es.networkersmc.authplugin.session;

import com.google.common.collect.Maps;
import es.networkersmc.authplugin.data.AuthenticationDataDAO;
import es.networkersmc.authplugin.data.AuthenticationDataRepository;
import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.authplugin.event.LoginFailureEvent;
import es.networkersmc.authplugin.event.LoginSuccessEvent;
import es.networkersmc.authplugin.event.PasswordInputEvent;
import es.networkersmc.authplugin.security.EncryptionService;
import es.networkersmc.authplugin.security.PasswordRequirementUtil;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.docs.User;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.util.bukkit.concurrent.MinecraftExecutor;
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

    @Inject private EncryptionService encryptionService;

    @Inject private MinecraftExecutor minecraftExecutor;
    @Inject private ExecutorService asyncExecutor;

    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private AuthenticationDataDAO dataDAO;

    @Inject private EventService eventService;
    @Inject private PlayerLanguageService languageService;

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

    public void loginAsync(Player player, AuthSession session, String password) {
        async(() -> this.loginSync(player, session, password));
    }

    public void loginSync(Player player, AuthSession session, String password) {
        boolean correctPassword = encryptionService.verify(password, session.getData().getPasswordHash());

        if (correctPassword) {
            this.forceLogin(player, session);
        } else {
            sync(() -> eventService.callEvent(new LoginFailureEvent(player, session)));
        }
    }

    public void registerAsync(Player player, AuthSession session, String password) {
        async(() -> this.registerSync(player, session, password));
    }

    public void registerSync(Player player, AuthSession session, String password) {
        AuthState currentState = session.getState();

        if (currentState == AuthState.REGISTER || currentState == AuthState.CHANGE_PASSWORD) {
            boolean changingPassword = currentState == AuthState.CHANGE_PASSWORD;

            if (!PasswordRequirementUtil.isValid(password)) {
                languageService.sendMessage(player, session.getUser(), "auth.password-not-valid");
                return;
            }

            session.setBuffer(encryptionService.hash(password.toCharArray()));
            sync(() -> {
                eventService.callEvent(new PasswordInputEvent(player, session, changingPassword));
                session.setState(changingPassword ? AuthState.CHANGE_PASSWORD_CONFIRM : AuthState.REGISTER_CONFIRM);
            });
        }

        if (currentState == AuthState.REGISTER_CONFIRM || currentState == AuthState.CHANGE_PASSWORD_CONFIRM) {
            boolean changingPassword = currentState == AuthState.CHANGE_PASSWORD_CONFIRM;

            char[] hash = session.getBuffer();
            session.setBuffer(new char[0]);

            boolean passwordsMatch = encryptionService.verify(password.toCharArray(), hash);

            if (!passwordsMatch) {
                sync(() -> {
                    eventService.callEvent(new LoginFailureEvent(player, session));
                    session.setState(changingPassword ? AuthState.CHANGE_PASSWORD : AuthState.REGISTER);
                });
            } else {
                this.registerSync(player, session, hash);
            }
        }
    }

    public void registerAsync(Player player, AuthSession session, char[] hash) {
        async(() -> this.registerSync(player, session, hash));
    }

    public void registerSync(Player player, AuthSession session, char[] hash) {
        AuthenticationData data = session.getData();
        data.setPasswordHash(new String(hash));
        dataRepository.updateSync(data);

        this.forceLogin(player, session);
    }

    private void forceLogin(Player player, AuthSession session) {
        sync(() -> {
            eventService.callEvent(new LoginSuccessEvent(player));
            session.setState(AuthState.LOGGED_IN);
        });
    }

    private void async(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

    private void sync(Runnable runnable) {
        minecraftExecutor.execute(runnable);
    }

}
