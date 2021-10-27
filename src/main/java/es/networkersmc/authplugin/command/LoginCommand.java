package es.networkersmc.authplugin.command;

import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.authplugin.session.AuthSessionService;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.command.Command;
import es.networkersmc.dendera.command.annotation.HelpSubCommand;
import es.networkersmc.dendera.command.annotation.NoArgsSubCommand;
import es.networkersmc.dendera.command.annotation.parameter.Parameters;
import es.networkersmc.dendera.command.annotation.parameter.Sender;
import es.networkersmc.dendera.docs.User;
import es.networkersmc.dendera.minecraft.command.annotation.rule.AllowedExecutor;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.List;

@AllowedExecutor(Player.class)
public class LoginCommand extends Command {

    @Inject private PlayerLanguageService languageService;
    @Inject private AuthSessionService sessionService;

    public LoginCommand() {
        super("login");
    }

    @NoArgsSubCommand
    public void noArgs(@Sender Player player, User user) {
        languageService.sendMessage(player, user, "auth.command.login.deprecated");
    }

    @HelpSubCommand // The subCommand is actually the password
    public void onPasswordInput(@Sender Player player, User user, @Parameters List<String> parameters) {
        if (parameters.size() > 1) {
            languageService.sendMessage(player, user, "auth.password-contains-spaces");
            return;
        }

        AuthSession session = sessionService.getSession(player.getUniqueId());
        String password = parameters.get(0);
        sessionService.loginAsync(player, session, password);
    }

}
