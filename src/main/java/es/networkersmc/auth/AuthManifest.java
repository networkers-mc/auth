package es.networkersmc.auth;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import es.networkersmc.auth.command.CommandsManifest;
import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.auth.flow.BannerService;
import es.networkersmc.auth.flow.FlowPlayerController;
import es.networkersmc.auth.player.PlayerController;
import es.networkersmc.auth.security.SecurityManifest;
import es.networkersmc.auth.flow.WorldController;
import es.networkersmc.dendera.inject.Manifest;
import es.networkersmc.dendera.util.CooldownManager;
import es.networkersmc.dendera.util.inject.ParametrizedType;
import org.bukkit.entity.Player;

public class AuthManifest extends Manifest {

    private final AuthPlugin plugin;

    public AuthManifest(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        install(new CommandsManifest());
        install(new SecurityManifest());

        bindModel(AuthenticationData.class);

        bindModule(BannerService.class);
        bindModule(FlowPlayerController.class);
        bindModule(PlayerController.class);
        bindModule(WorldController.class);

        bind(ParametrizedType.getType(CooldownManager.class, Player.class))
                .annotatedWith(Names.named("auth-cooldown-manager"))
                .in(Scopes.SINGLETON);
    }
}
