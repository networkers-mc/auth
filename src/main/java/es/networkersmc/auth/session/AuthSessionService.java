package es.networkersmc.auth.session;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import es.networkersmc.auth.data.AuthenticationDataRepository;
import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.auth.security.EncryptionService;
import es.networkersmc.dendera.minecraft.switchboard.SwitchboardService;
import es.networkersmc.dendera.switchboard.bukkit.NodeType;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
public class AuthSessionService {

    @Inject private ExecutorService executorService;

    @Inject private EncryptionService encryptionService;
    @Inject private AuthenticationDataRepository dataRepository;
    @Inject private SwitchboardService switchboardService;

    /**
     * Checks if the given password is the provided user's password.
     *
     * @return {@code true} if the password is valid, {@code false} otherwise.
     */
    public boolean verifyPasswordSync(AuthSession session, String input) {
        return encryptionService.verify(input, session.getData().getPasswordHash());
    }

    public void registerSync(AuthSession session, char[] hash) { // asks for the hashed password because Flow stores it hashed
        AuthenticationData data = session.getData();
        data.setPasswordHash(new String(hash));
        dataRepository.updateSync(data);
    }

    public ListenableFuture<Void> registerAsync(AuthSession session, char[] hash) {
        SettableFuture<Void> future = SettableFuture.create();
        executorService.submit(() -> {
            this.registerSync(session, hash);
            future.set(null);
        });
        return future;
    }

    public ListenableFuture<Void> verifyPasswordAsync(AuthSession session, String input) {
        SettableFuture<Void> future = SettableFuture.create();
        executorService.submit(() -> {
            if (this.verifyPasswordSync(session, input))
                future.set(null);
            else
                future.setException(new IllegalAccessException());
        });
        return future;
    }

    public void sendToHub(Player player) {
        switchboardService.connectUserToBestNode(player.getUniqueId(), NodeType.HUB);
    }
}
