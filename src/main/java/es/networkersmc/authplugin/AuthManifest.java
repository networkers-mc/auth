package es.networkersmc.authplugin;

import com.google.inject.name.Names;
import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.authplugin.flow.FlowManifest;
import es.networkersmc.authplugin.security.SecurityManifest;
import es.networkersmc.dendera.inject.Manifest;

import java.io.File;
import java.lang.annotation.Annotation;

public class AuthManifest extends Manifest {

    private final AuthPlugin plugin;

    public AuthManifest(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        install(new FlowManifest());
        install(new SecurityManifest());

        bindModel(AuthenticationData.class);

        bindPluginFile("banners", Names.named("banners-directory"));
    }

    private void bindPluginFile(String fileName, Annotation annotation) {
        File bannersDirectory = new File(plugin.getDataFolder(), fileName);
        bind(File.class).annotatedWith(annotation).toInstance(bannersDirectory);
    }
}
