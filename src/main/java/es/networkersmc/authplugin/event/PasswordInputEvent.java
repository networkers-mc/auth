package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthState;
import es.networkersmc.dendera.bukkit.event.player.PlayerEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called when a player is registering or changing their password and enters a new password.
 */
@Getter
public class PasswordInputEvent extends PlayerEvent {

    private final AuthState authState;

    public PasswordInputEvent(Player player, AuthState authState) {
        super(player);
        this.authState = authState;
    }
}
