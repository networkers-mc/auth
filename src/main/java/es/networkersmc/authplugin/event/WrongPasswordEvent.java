package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthState;
import es.networkersmc.dendera.bukkit.event.player.PlayerEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called when a player tries to log in with a wrong password or the passwords don't match while registering.
 */
@Getter
public class WrongPasswordEvent extends PlayerEvent {

    private final AuthState authState;

    public WrongPasswordEvent(Player player, AuthState authState) {
        super(player);
        this.authState = authState;
    }
}
