package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthSession;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called when a player is registering or changing their password and the auth state changes to CONFIRM
 */
@Getter
public class PasswordInputErrorEvent extends AuthEvent {

    private final boolean changingPassword;

    public PasswordInputErrorEvent(Player player, AuthSession session, boolean changingPassword) {
        super(player, session);
        this.changingPassword = changingPassword;
    }
}
