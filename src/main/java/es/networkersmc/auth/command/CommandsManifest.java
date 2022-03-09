package es.networkersmc.auth.command;

import es.networkersmc.dendera.inject.Manifest;

public class CommandsManifest extends Manifest {
    @Override
    protected void configure() {
        bindCommand(LoginCommand.class);
        bindCommand(RegisterCommand.class);
    }
}
