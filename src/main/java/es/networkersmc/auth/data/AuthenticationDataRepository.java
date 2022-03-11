package es.networkersmc.auth.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import es.networkersmc.auth.docs.AuthenticationData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class AuthenticationDataRepository {

    @Inject private ListeningExecutorService executorService;
    @Inject private AuthenticationDataDAO authenticationDataDAO;

    public Optional<AuthenticationData> getSync(UUID uuid) {
        return authenticationDataDAO.get(uuid);
    }

    public ListenableFuture<AuthenticationData> get(UUID uuid) {
        return executorService.submit(() -> this.getSync(uuid).get());
    }

    public void updateSync(AuthenticationData data) {
        authenticationDataDAO.update(data);
    }

    public ListenableFuture<?> updateAsync(AuthenticationData data) {
        return executorService.submit(() -> this.updateSync(data));
    }
}
