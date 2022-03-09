package es.networkersmc.auth.docs;

import es.networkersmc.dendera.database.annotation.CollectionName;
import es.networkersmc.dendera.database.annotation.DatabaseName;
import es.networkersmc.dendera.document.SimpleEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@DatabaseName("denderadb")
@CollectionName("authenticationdata")
public class AuthenticationData extends SimpleEntity {

    @Getter @Setter private String passwordHash;

    public AuthenticationData() {
        // Constructor for Jackson
    }

    public AuthenticationData(UUID uuid) {
        super(uuid);
    }
}
