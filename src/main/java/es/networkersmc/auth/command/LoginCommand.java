package es.networkersmc.auth.command;

import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.auth.session.AuthSessionCache;
import es.networkersmc.auth.session.AuthSessionService;
import es.networkersmc.auth.session.AuthState;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.command.Command;
import es.networkersmc.dendera.command.annotation.HelpSubCommand;
import es.networkersmc.dendera.command.annotation.NoArgsSubCommand;
import es.networkersmc.dendera.command.annotation.parameter.Parameters;
import es.networkersmc.dendera.command.annotation.parameter.Sender;
import es.networkersmc.dendera.docs.User;
import es.networkersmc.dendera.minecraft.command.annotation.rule.AllowedExecutor;
import es.networkersmc.dendera.util.CooldownManager;
import es.networkersmc.dendera.util.future.FutureUtils;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllowedExecutor(Player.class)
public class LoginCommand extends Command {

    @Inject private @Named("auth-cooldown-manager") CooldownManager<Player> cooldownManager;
    @Inject private PlayerLanguageService languageService;

    @Inject private AuthSessionCache sessionCache;
    @Inject private AuthSessionService sessionService;

    public LoginCommand() {
        super("login");
    }

    @NoArgsSubCommand
    public void login(@Sender Player player, User user) {
        this.sendDeprecatedMessage(player, user);
    }

    @HelpSubCommand // The subCommand is actually the password
    public void onPasswordInput(@Sender Player player, User user, @Parameters List<String> parameters) {
        if (!cooldownManager.hasElapsed(player, 100, ChronoUnit.MILLIS)) {
            return;
        }
        cooldownManager.update(player);

        if (parameters.size() < 1) {
            this.sendDeprecatedMessage(player, user, "auth.command.login.usage");
            return;
        }

        if (parameters.size() > 1) {
            this.sendDeprecatedMessage(player, user, "auth.password-contains-spaces");
            return;
        }

        AuthSession session = sessionCache.getSession(player.getUniqueId());
        if (session.getState() != AuthState.LOGIN) {
            this.sendDeprecatedMessage(player, user);
            return;
        }

        String password = parameters.get(0);

        FutureUtils.addCallback(
                sessionService.assertThatPasswordMatches(session, password),
                __ -> sessionService.sendToHub(player),
                __ -> this.sendDeprecatedMessage(player, user, "auth.command.login.wrong-password")
        );
    }

    private void sendDeprecatedMessage(Player player, User user) {
        languageService.sendMessage(player, user, "auth.command.login.deprecated");
    }

    private void sendDeprecatedMessage(Player player, User user, String anotherMessageId, String... parameters) {
        this.sendDeprecatedMessage(player, user);
        languageService.sendMessage(player, user, anotherMessageId, parameters);
    }
}
