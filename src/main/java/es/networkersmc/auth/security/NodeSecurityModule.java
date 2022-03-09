package es.networkersmc.auth.security;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.auth.session.AuthSessionService;
import es.networkersmc.auth.session.AuthState;
import es.networkersmc.dendera.module.Module;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

/**
 * Module to manage the Minecraft node security.
 */
public class NodeSecurityModule implements Module, Listener {

    private final Set<String> allowedCommands = Sets.newHashSet();

    @Inject private AuthSessionService sessionService;
    @Inject private Server bukkit;

    @Override
    public void onStart() {
        this.allowCommands("register", "login");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        AuthSession session = sessionService.getSession(player.getUniqueId());

        if (session.getState() == AuthState.LOGGED_IN) {
            return;
        }

        String command = event.getMessage()
                .substring(1) // Remove the slash
                .split(" ", 2)[0]; // Just get the actual command, not the parameters

        if (allowedCommands.contains(command.toLowerCase())) {
            //TODO: message
            event.setCancelled(true);
        }
    }

    private void allowCommands(String... commands) {
        for (String command : commands) {
            allowedCommands.add(command.toLowerCase());

            bukkit.getPluginCommand(command).getAliases().stream()
                    .map(String::toLowerCase)
                    .forEach(allowedCommands::add);
        }
    }

}
