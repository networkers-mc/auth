package es.networkersmc.authplugin.flow;

import es.networkersmc.authplugin.security.EncryptionService;
import es.networkersmc.authplugin.security.PasswordRequirementUtil;
import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.authplugin.session.AuthSessionService;
import es.networkersmc.authplugin.session.AuthState;
import es.networkersmc.dendera.bukkit.event.player.UserLoginEvent;
import es.networkersmc.dendera.bukkit.event.player.UserPreLoginEvent;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.CooldownManager;
import es.networkersmc.dendera.util.bukkit.concurrent.MinecraftExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

public class PlayerListenerModule implements Module, Listener {

    @Inject private MinecraftExecutor minecraftExecutor;
    @Inject private EventService eventService;
    @Inject private PlayerLanguageService languageService;

    @Inject private EncryptionService encryptionService;
    @Inject private AuthSessionService authSessionService;
    @Inject private BannerControllerModule bannerControllerModule;

    private final CooldownManager<Player> cooldownManager = new CooldownManager<>();

    @Override
    public void onStart() {
        eventService.registerListener(UserPreLoginEvent.class, event -> {
            if (event.getResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                authSessionService.load(event.getUser());
            }
        });

        eventService.registerListener(UserLoginEvent.class, event -> {
            if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
                authSessionService.unload(event.getPlayer().getUniqueId());
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (cooldownManager.hasElapsed(player, 100, ChronoUnit.MILLIS)) {
            AuthSession session = authSessionService.getSession(player.getUniqueId());
            this.onPlayerInput(player, session, event.getMessage());
            cooldownManager.update(player);
        }
        event.setMessage("");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        authSessionService.unload(player.getUniqueId());
        cooldownManager.remove(player);
    }

    private void onPlayerInput(Player player, AuthSession session, String input) {
        if (input.contains(" ")) {
            languageService.sendMessage(player, session.getUser(), "auth.password-contains-spaces");
            return;
        }

        AuthState currentState = session.getState();

        switch (currentState) {
            case LOGGED_IN:
                break;

            case LOGIN:
                boolean success = authSessionService.loginSync(session, input);

                sync(() -> {
                    if (success) {
                        session.setState(AuthState.LOGGED_IN);
                        authSessionService.forceLogin(player);

                        bannerControllerModule.onLoginSuccess(player);
                    } else {
                        bannerControllerModule.onLoginFailure(player, session);
                    }
                });
                break;

            case REGISTER:
            case CHANGE_PASSWORD: {
                boolean isChangingPassword = currentState == AuthState.CHANGE_PASSWORD;

                if (!PasswordRequirementUtil.isValid(input)) {
                    languageService.sendMessage(player, session.getUser(), "auth.password-not-valid");
                    return;
                }

                session.setBuffer(encryptionService.hash(input.toCharArray()));
                sync(() -> {
                    bannerControllerModule.onPasswordInput(player, session);
                    session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD_CONFIRM : AuthState.REGISTER_CONFIRM);
                });
                return;
            }

            case REGISTER_CONFIRM:
            case CHANGE_PASSWORD_CONFIRM: {
                boolean isChangingPassword = currentState == AuthState.CHANGE_PASSWORD_CONFIRM;

                char[] hash = session.getBuffer();
                boolean passwordsMatch = encryptionService.verify(input.toCharArray(), hash);

                if (!passwordsMatch) {
                    sync(() -> {
                        bannerControllerModule.onPasswordInputError(player, session, isChangingPassword);
                        session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD : AuthState.REGISTER);
                    });
                } else {
                    session.setState(AuthState.LOGGED_IN);
                    authSessionService.registerSync(session, hash);
                    authSessionService.forceLogin(player);
                }

                for (int i = 0; i < hash.length; i++) {
                    hash[0] = 0x00;
                }
                session.setBuffer(null);
            }
        }
    }

    private void sync(Runnable runnable) {
        minecraftExecutor.execute(runnable);
    }
}