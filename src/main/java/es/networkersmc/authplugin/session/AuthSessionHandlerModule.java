package es.networkersmc.authplugin.session;

import com.google.common.collect.Maps;
import es.networkersmc.authplugin.data.AuthenticationDataDAO;
import es.networkersmc.authplugin.data.AuthenticationDataRepository;
import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.authplugin.event.LoginSuccessEvent;
import es.networkersmc.authplugin.event.PasswordInputEvent;
import es.networkersmc.authplugin.event.WrongPasswordEvent;
import es.networkersmc.authplugin.security.EncryptionService;
import es.networkersmc.dendera.bukkit.event.player.UserLoginEvent;
import es.networkersmc.dendera.bukkit.event.player.UserPreLoginEvent;
import es.networkersmc.dendera.event.DenderaEvent;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.bukkit.concurrent.MinecraftExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class AuthSessionHandlerModule implements Module, Listener {

    private final Map<UUID, AuthSession> sessions = Maps.newConcurrentMap();

    @Inject private EncryptionService encryptionService;

    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private AuthenticationDataDAO dataDAO;

    @Inject private EventService eventService;

    @Override
    public void onStart() {
        eventService.registerListener(UserPreLoginEvent.class, event -> {
            this.load(event.getUser().getUUID());
        });

        eventService.registerListener(UserLoginEvent.class, event -> {
            if (event.getResult() != Result.ALLOWED) {
                sessions.remove(event.getPlayer().getUniqueId());
            }
        });
    }

    public AuthSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    // ----------------------------
    // SESSION HANDLER
    // ----------------------------

    // Info: Called asynchronously from UserPreLoginEvent
    // Don't worry about sync calls to database
    private void load(UUID uuid) {
        AuthSession session = dataRepository.getSync(uuid)
                .map(data -> new AuthSession(AuthState.LOGIN, data))
                .orElseGet(() -> new AuthSession(AuthState.REGISTER, dataDAO.create(uuid)));

        sessions.put(uuid, session);
    }

    // Info: Called asynchronously from AsyncPlayerChatEvent
    // Don't worry about sync calls to database
    private void register(Player player, AuthSession session, String password) {
        AuthState currentState = session.getState();

        if (currentState == AuthState.REGISTER || currentState == AuthState.CHANGE_PASSWORD) {
            session.setBuffer(password);
            this.callEventSynchronously(new PasswordInputEvent(player, currentState));

            session.setState(currentState == AuthState.REGISTER
                    ? AuthState.REGISTER_CONFIRM
                    : AuthState.CHANGE_PASSWORD_CONFIRM);
        }

        if (currentState == AuthState.REGISTER_CONFIRM || currentState == AuthState.CHANGE_PASSWORD_CONFIRM) {
            String password0 = session.getBuffer();

            if (!password0.equals(password)) {
                this.callEventSynchronously(new WrongPasswordEvent(player, currentState));

                session.setState(currentState == AuthState.REGISTER_CONFIRM
                        ? AuthState.REGISTER
                        : AuthState.CHANGE_PASSWORD);
            } else {
                String passwordHash = encryptionService.hash(password);

                AuthenticationData data = session.getData();
                data.setPasswordHash(passwordHash);
                dataRepository.updateSync(data);

                this.forceLogin(player, session);
            }
        }
    }

    // Info: Called asynchronously from AsyncPlayerChatEvent
    // Don't worry about sync calls to database
    private void login(Player player, AuthSession session, String password) {
        boolean correctPassword = encryptionService.verify(password, session.getData().getPasswordHash());

        if (correctPassword) {
            this.forceLogin(player, session);
        } else {
            this.callEventSynchronously(new WrongPasswordEvent(player, session.getState()));
        }
    }

    private void forceLogin(Player player, AuthSession session) {
        this.callEventSynchronously(new LoginSuccessEvent(player));
        session.setState(AuthState.LOGGED_IN);
    }

    private void onPlayerInput(Player player, AuthSession session, String input) {
        switch (session.getState()) {
            case LOGGED_IN:
                break;

            case LOGIN:
                this.login(player, session, input);
                break;

            case REGISTER:
            case REGISTER_CONFIRM:
            case CHANGE_PASSWORD:
            case CHANGE_PASSWORD_CONFIRM: // Not "default:" to prevent bugs if a new state is added to the enum
                this.register(player, session, input);
                break;
        }
    }

    // ----------------------------
    // BUKKIT EVENT LISTENERS
    // ----------------------------

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        AuthSession session = sessions.get(player.getUniqueId());

        this.onPlayerInput(player, session, event.getMessage());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    // ----------------------------
    // UTILS
    // ----------------------------

    @Inject private MinecraftExecutor minecraftExecutor;

    private void callEventSynchronously(DenderaEvent event) {
        minecraftExecutor.execute(() -> eventService.callEvent(event));
    }

}
