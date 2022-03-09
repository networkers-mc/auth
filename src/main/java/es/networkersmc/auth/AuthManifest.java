package es.networkersmc.auth;

import com.google.inject.name.Names;
import es.networkersmc.auth.command.CommandsManifest;
import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.auth.flow.FlowManifest;
import es.networkersmc.auth.security.SecurityManifest;
import es.networkersmc.dendera.inject.Manifest;
import es.networkersmc.dendera.util.CooldownManager;
import es.networkersmc.dendera.util.inject.ParametrizedType;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.annotation.Annotation;

public class AuthManifest extends Manifest {

    private final AuthPlugin plugin;

    public AuthManifest(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        install(new CommandsManifest());
        install(new FlowManifest());
        install(new SecurityManifest());

        bindModel(AuthenticationData.class);
        bindPluginFile("banners", Names.named("banners-directory"));

        bind(ParametrizedType.getType(CooldownManager.class, Player.class))
                .annotatedWith(Names.named("auth-cooldown-manager"))
                .toInstance(new CooldownManager<Player>());
    }

    private void bindPluginFile(String fileName, Annotation annotation) {
        File bannersDirectory = new File(plugin.getDataFolder(), fileName);
        bind(File.class).annotatedWith(annotation).toInstance(bannersDirectory);
    }
}
