package es.networkersmc.authplugin;

import es.networkersmc.dendera.DenderaInjector;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        DenderaInjector.getInstance().registerModule(new AuthManifest(this));
    }
}
