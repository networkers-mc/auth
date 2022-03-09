package es.networkersmc.auth.security;

import com.google.inject.Scopes;
import es.networkersmc.dendera.inject.Manifest;

public class SecurityManifest extends Manifest {

    @Override
    protected void configure() {
        bind(EncryptionService.class).to(BCryptEncryptionService.class).in(Scopes.SINGLETON);

        bindModule(NodeSecurityModule.class);
    }
}
