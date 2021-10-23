package es.networkersmc.authplugin.event;

import es.networkersmc.dendera.bukkit.event.player.PlayerEvent;
import org.bukkit.entity.Player;

/**
 * Called when a player successfully logs in.
 */
public class LoginSuccessEvent extends PlayerEvent {

    public LoginSuccessEvent(Player player) {
        super(player);
    }
}
