package es.networkersmc.authplugin.event;

import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.dendera.bukkit.event.player.PlayerEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class AuthEvent extends PlayerEvent {

    private final AuthSession session;

    public AuthEvent(Player player, AuthSession session) {
        super(player);
        this.session = session;
    }
}
