package es.networkersmc.auth.player;

import es.networkersmc.auth.data.AuthenticationDataDAO;
import es.networkersmc.auth.data.AuthenticationDataRepository;
import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.auth.session.AuthSessionCache;
import es.networkersmc.auth.session.AuthState;
import es.networkersmc.dendera.bukkit.event.player.UserLoginEvent;
import es.networkersmc.dendera.bukkit.event.player.UserPreLoginEvent;
import es.networkersmc.dendera.docs.User;
import es.networkersmc.dendera.event.EventService;
import es.networkersmc.dendera.module.Module;
import es.networkersmc.dendera.util.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

public class PlayerController implements Module, Listener {

    @Inject private EventService eventService;
    @Inject private @Named("auth-cooldown-manager") CooldownManager<Player> cooldownManager;

    @Inject private AuthenticationDataDAO dataDAO;
    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private AuthSessionCache authSessionCache;

    @Override
    public void onStart() {
        eventService.registerListener(UserPreLoginEvent.class, this::onUserPreLogin);
        eventService.registerListener(UserLoginEvent.class, this::onUserLogin);
    }

    private void onUserPreLogin(UserPreLoginEvent event) {
        if (event.getResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        User user = event.getUser();
        UUID uuid = user.getUUID();
        AuthSession session = dataRepository.getSync(uuid)
                .map(data -> new AuthSession(user, data, AuthState.LOGIN))
                .orElseGet(() -> new AuthSession(user, dataDAO.create(uuid), AuthState.REGISTER));

        authSessionCache.cache(uuid, session);
    }

    private void onUserLogin(UserLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            authSessionCache.remove(event.getUser().getUUID());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cooldownManager.remove(player);
        authSessionCache.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        event.setCancelled(true);
    }
}
