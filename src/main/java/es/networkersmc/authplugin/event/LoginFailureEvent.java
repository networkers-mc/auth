package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthSession;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called when a player tries to log in with a wrong password or the passwords don't match while registering.
 */
@Getter
public class LoginFailureEvent extends AuthEvent {

    public LoginFailureEvent(Player player, AuthSession session) {
        super(player, session);
    }
}
