package es.networkersmc.auth.flow;

import es.networkersmc.auth.session.AuthSessionCache;
import es.networkersmc.dendera.bukkit.event.player.UserJoinEvent;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.temporal.ChronoUnit;

public class FlowPlayerController implements Module, Listener {

    @Inject private @Named("auth-cooldown-manager") CooldownManager<Player> cooldownManager;
    @Inject private EventService eventService;

    @Inject private AuthSessionCache authSessionCache;
    @Inject private FlowService flowService;
    @Inject private BannerService bannerService;

    @Override
    public void onStart() {
        eventService.registerListener(UserJoinEvent.class, this::onUserJoin);
    }

    private void onUserJoin(UserJoinEvent event) {
        Player player = event.getPlayer();
        bannerService.setup(player, authSessionCache.getSession(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (cooldownManager.hasElapsed(player, 100, ChronoUnit.MILLIS)) {
            flowService.handlePlayerInput(player, authSessionCache.getSession(player.getUniqueId()), event.getMessage());
            cooldownManager.update(player);
        }
        event.setMessage("");
    }

    @EventHandler
    public void onLeaveLock(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
            event.getPlayer().setSpectatorTarget(bannerService.getLock());
        }
    }
}