package es.networkersmc.auth.flow;

import es.networkersmc.auth.security.EncryptionService;
import es.networkersmc.auth.security.PasswordRequirements;
import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.auth.session.AuthSessionService;
import es.networkersmc.auth.session.AuthState;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
import es.networkersmc.dendera.minecraft.documentation.Async;
import es.networkersmc.dendera.util.bukkit.concurrent.MinecraftExecutor;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FlowService {

    @Inject private MinecraftExecutor minecraftExecutor;
    @Inject private PlayerLanguageService languageService;

    @Inject private AuthSessionService authSessionService;
    @Inject private EncryptionService encryptionService;
    @Inject private BannerService bannerService;

    @Async // from chat event
    public void handlePlayerInput(Player player, AuthSession session, String input) {
        if (input.contains(" ")) {
            languageService.sendMessage(player, session.getUser(), "auth.password-contains-spaces");
            return;
        }

        switch (session.getState()) {
            case LOGIN: this.handleLogin(player, session, input); break;
            case REGISTER:
            case CHANGE_PASSWORD: this.handleFirstRegisterPassword(player, session, input); break;
            case REGISTER_CONFIRM:
            case CHANGE_PASSWORD_CONFIRM: this.handlePasswordConfirm(player, session, input); break;
        }
    }

    public void handleLogin(Player player, AuthSession session, String password) {
        try {
            authSessionService.assertThatPasswordMatchesSync(session, password);

            session.setState(AuthState.LOGGED_IN);
            authSessionService.sendToHub(player);
            sync(() -> bannerService.displayLoggedIn(player));
        } catch (IllegalArgumentException e) {
            sync(() -> bannerService.displayWrongPassword(player, session));
        }
    }

    public void handleFirstRegisterPassword(Player player, AuthSession session, String password) {
        boolean isChangingPassword = session.getState() == AuthState.CHANGE_PASSWORD;

        if (!PasswordRequirements.isValid(password)) {
            languageService.sendMessage(player, session.getUser(), "auth.password-not-valid");
            return;
        }

        session.setBuffer(encryptionService.hash(password.toCharArray()));
        session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD_CONFIRM : AuthState.REGISTER_CONFIRM);
        sync(() -> bannerService.displayConfirmPassword(player, session));
    }

    public void handlePasswordConfirm(Player player, AuthSession session, String password) {
        boolean isChangingPassword = session.getState() == AuthState.CHANGE_PASSWORD_CONFIRM;
        boolean passwordsMatch = encryptionService.verify(password.toCharArray(), session.getBuffer());

        if (!passwordsMatch) {
            session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD : AuthState.REGISTER);
            sync(() -> bannerService.displayPasswordsDontMatch(player, session, isChangingPassword));
        } else {
            session.setState(AuthState.LOGGED_IN);
            authSessionService.registerSync(session, session.getBuffer());
            authSessionService.sendToHub(player);
        }

        session.clearBuffer();
    }

    private void sync(Runnable runnable) {
        minecraftExecutor.execute(runnable);
    }
}
