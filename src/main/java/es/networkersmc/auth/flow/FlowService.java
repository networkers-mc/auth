package es.networkersmc.auth.flow;

import es.networkersmc.auth.security.EncryptionService;
import es.networkersmc.auth.security.PasswordRequirements;
import es.networkersmc.auth.session.AuthSession;
import es.networkersmc.auth.session.AuthSessionService;
import es.networkersmc.auth.session.AuthState;
import es.networkersmc.dendera.bukkit.language.PlayerLanguageService;
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
        boolean success = authSessionService.verifyPasswordSync(session, password);

        sync(() -> {
            if (success) {
                session.setState(AuthState.LOGGED_IN);
                authSessionService.sendToHub(player);

                bannerService.displayLoggedIn(player);
            } else {
                bannerService.displayWrongPassword(player, session);
            }
        });
    }

    public void handleFirstRegisterPassword(Player player, AuthSession session, String password) {
        boolean isChangingPassword = session.getState() == AuthState.CHANGE_PASSWORD;

        if (!PasswordRequirements.isValid(password)) {
            languageService.sendMessage(player, session.getUser(), "auth.password-not-valid");
            return;
        }

        session.setBuffer(encryptionService.hash(password.toCharArray()));
        sync(() -> {
            bannerService.displayConfirmPassword(player, session);
            session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD_CONFIRM : AuthState.REGISTER_CONFIRM);
        });
    }

    public void handlePasswordConfirm(Player player, AuthSession session, String password) {
        boolean isChangingPassword = session.getState() == AuthState.CHANGE_PASSWORD_CONFIRM;

        char[] hash = session.getBuffer();
        boolean passwordsMatch = encryptionService.verify(password.toCharArray(), hash);

        if (!passwordsMatch) {
            sync(() -> {
                bannerService.displayPasswordsDontMatch(player, session, isChangingPassword);
                session.setState(isChangingPassword ? AuthState.CHANGE_PASSWORD : AuthState.REGISTER);
            });
        } else {
            session.setState(AuthState.LOGGED_IN);
            authSessionService.registerSync(session, hash);
            authSessionService.sendToHub(player);
        }

        for (int i = 0; i < hash.length; i++) {
            hash[0] = 0x00;
        }
        session.setBuffer(null);
    }

    private void sync(Runnable runnable) {
        minecraftExecutor.execute(runnable);
    }
}
