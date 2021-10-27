package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthSession;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called when passwords don't match when registering or changing password
 */
@Getter
public class PasswordInputErrorEvent extends AuthEvent {

    private final boolean changingPassword;

    public PasswordInputErrorEvent(Player player, AuthSession session, boolean changingPassword) {
        super(player, session);
        this.changingPassword = changingPassword;
    }
}
