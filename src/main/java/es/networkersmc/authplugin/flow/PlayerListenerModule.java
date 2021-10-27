package es.networkersmc.authplugin.flow;

import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.authplugin.session.AuthSessionService;
import es.networkersmc.dendera.bukkit.event.player.UserLoginEvent;
import es.networkersmc.dendera.bukkit.event.player.UserPreLoginEvent;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

public class PlayerListenerModule implements Module, Listener {

    @Inject private AuthSessionService authSessionService;

    @Inject private EventService eventService;
    @Inject private PlayerLanguageService languageService;

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

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (cooldownManager.hasElapsed(player, 100, ChronoUnit.MILLIS)) {
            AuthSession session = authSessionService.getSession(player.getUniqueId());
            this.onPlayerInput(player, session, event.getMessage());
            cooldownManager.update(player);
        }
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

        switch (session.getState()) {
            case LOGGED_IN:
                break;

            case LOGIN:
                authSessionService.loginSync(player, session, input);
                break;

            case REGISTER:
            case REGISTER_CONFIRM:
            case CHANGE_PASSWORD:
            case CHANGE_PASSWORD_CONFIRM: // Not "default:" to prevent security errors if a new state is added to the enum
                authSessionService.registerSync(player, session, input);
                break;
        }
    }
}
