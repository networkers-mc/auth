package es.networkersmc.auth.session;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import es.networkersmc.auth.data.AuthenticationDataRepository;
import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.auth.security.EncryptionService;
import es.networkersmc.dendera.minecraft.switchboard.SwitchboardService;
import es.networkersmc.dendera.switchboard.bukkit.NodeType;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

// Has both sync and async solutions because we have to support chat (async event) and commands (sync).
@Singleton
public class AuthSessionService {

    @Inject private ListeningExecutorService executorService;

    @Inject private EncryptionService encryptionService;
    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private SwitchboardService switchboardService;

    /**
     * Sets the given password hash to the user. Saves into database.
     */
    public void registerSync(AuthSession session, char[] hash) { // asks for the hashed password because it's buffered hashed
        AuthenticationData data = session.getData();
        data.setPasswordHash(new String(hash));
        dataRepository.updateSync(data);
    }

    public ListenableFuture<?> registerAsync(AuthSession session, char[] hash) {
        return executorService.submit(() -> this.registerSync(session, hash));
    }

    /**
     * Checks if the given password is the provided user's password. Throws {@link IllegalArgumentException} if not.
     */
    public void assertThatPasswordMatchesSync(AuthSession session, String input) throws IllegalArgumentException {
        if (!encryptionService.verify(input, session.getData().getPasswordHash()))
            throw new IllegalArgumentException("invalid password");
    }

    public ListenableFuture<?> assertThatPasswordMatches(AuthSession session, String input) {
        return executorService.submit(() -> this.assertThatPasswordMatchesSync(session, input));
    }

    public void sendToHub(Player player) {
        switchboardService.connectUserToBestNode(player.getUniqueId(), NodeType.HUB);
    }
}
