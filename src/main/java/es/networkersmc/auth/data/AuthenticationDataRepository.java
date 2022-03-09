package es.networkersmc.auth.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.dendera.user.UserNotFoundException;
import es.networkersmc.dendera.util.NullableOptional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Singleton
public class AuthenticationDataRepository {

    @Inject private ExecutorService executorService;
    @Inject private AuthenticationDataDAO authenticationDataDAO;

    public ListenableFuture<AuthenticationData> get(UUID uuid) {
        SettableFuture<AuthenticationData> future = SettableFuture.create();

        executorService.submit(() -> {
            NullableOptional.of(this.getSync(uuid))
                    .ifPresent(future::set)
                    .orElse(() -> future.setException(new UserNotFoundException()));
        });
        return future;
    }

    public Optional<AuthenticationData> getSync(UUID uuid) {
        return authenticationDataDAO.get(uuid);
    }

    public ListenableFuture<Void> update(AuthenticationData data) {
        SettableFuture<Void> future = SettableFuture.create();

        executorService.submit(() -> {
            this.updateSync(data);
            future.set(null);
        });
        return future;
    }

    public void updateSync(AuthenticationData data) {
        authenticationDataDAO.update(data);
    }

}
