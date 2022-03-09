package es.networkersmc.auth.flow;

import es.networkersmc.dendera.inject.Manifest;

public class FlowManifest extends Manifest {

    @Override
    protected void configure() {
        bindModule(BannerControllerModule.class);
        bindModule(PlayerListenerModule.class);
        bindModule(WorldManagerModule.class);
    }
}
