package es.networkersmc.authplugin;

import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.authplugin.security.SecurityManifest;
import es.networkersmc.authplugin.session.AuthSessionHandlerModule;
import es.networkersmc.dendera.inject.Manifest;

public class AuthManifest extends Manifest {

    @Override
    protected void configure() {
        install(new SecurityManifest());

        bindModel(AuthenticationData.class);
        bindModule(AuthSessionHandlerModule.class);
    }
}
