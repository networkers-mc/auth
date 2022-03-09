package es.networkersmc.auth.data;

import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.dendera.database.DatabaseCollection;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class AuthenticationDataDAO {

    @Inject private DatabaseCollection<AuthenticationData> databaseCollection;

    public AuthenticationData create(UUID uuid) {
        return new AuthenticationData(uuid);
    }

    public Optional<AuthenticationData> get(UUID uuid) {
        return Optional.ofNullable(databaseCollection.get(uuid.toString()));
    }

    public void update(AuthenticationData data) {
        databaseCollection.update(data);
    }

    public void delete(String id) {
        databaseCollection.delete(id);
    }

}
