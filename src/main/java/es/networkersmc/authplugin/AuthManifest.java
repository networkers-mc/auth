package es.networkersmc.authplugin;

import es.networkersmc.authplugin.docs.AuthenticationData;
import es.networkersmc.dendera.inject.Manifest;

public class AuthManifest extends Manifest {

    @Override
    protected void configure() {
        bindModel(AuthenticationData.class);
    }
}
