package es.networkersmc.authplugin.command;

import es.networkersmc.authplugin.security.EncryptionService;
import es.networkersmc.authplugin.security.PasswordRequirementUtil;
import es.networkersmc.authplugin.session.AuthSession;
import es.networkersmc.authplugin.session.AuthSessionService;
import es.networkersmc.authplugin.session.AuthState;
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
public class RegisterCommand extends Command {

    @Inject private PlayerLanguageService languageService;

    @Inject private AuthSessionService sessionService;
    @Inject private EncryptionService encryptionService;

    @Inject private @Named("auth-cooldown-manager") CooldownManager<Player> cooldownManager;

    public RegisterCommand() {
        super("register");
    }

    @NoArgsSubCommand
    public void noArgs(@Sender Player player, User user) {
        this.sendDeprecatedMessage(player, user);
    }

    @HelpSubCommand // The subCommand is actually the password
    public void onPasswordInput(@Sender Player player, User user, @Parameters List<String> parameters) {
        if (!cooldownManager.hasElapsed(player, 100, ChronoUnit.MILLIS)) {
            return;
        }
        cooldownManager.update(player);

        if (parameters.size() < 2) {
            languageService.sendMessage(player, user, "auth.command.register.usage");
            return;
        }

        if (parameters.size() > 2) {
            languageService.sendMessage(player, user, "auth.password-contains-spaces");
            return;
        }

        AuthSession session = sessionService.getSession(player.getUniqueId());
        if (session.getState() != AuthState.REGISTER) {
            this.sendDeprecatedMessage(player, user);
            return;
        }

        String password1 = parameters.get(0);
        String password2 = parameters.get(1);
        boolean passwordsMatch = password1.equals(password2);

        if (!passwordsMatch) {
            //TODO: message
            return;
        }

        if (!PasswordRequirementUtil.isValid(password1)) {
            languageService.sendMessage(player, session.getUser(), "auth.password-not-valid");
            return;
        }

        session.setState(AuthState.LOGGED_IN);
        FutureUtils.onSuccess(
                sessionService.registerAsync(session, encryptionService.hash(password1.toCharArray())),
                onFinish -> sessionService.forceLogin(player)
        );
    }

    private void sendDeprecatedMessage(Player player, User user) {
        languageService.sendMessage(player, user, "auth.command.register.deprecated");
    }
}
