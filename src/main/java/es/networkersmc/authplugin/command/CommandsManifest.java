package es.networkersmc.authplugin.command;

import es.networkersmc.dendera.inject.Manifest;

public class CommandsManifest extends Manifest {
    @Override
    protected void configure() {
        bindCommand(LoginCommand.class);
        bindCommand(RegisterCommand.class);
    }
}
