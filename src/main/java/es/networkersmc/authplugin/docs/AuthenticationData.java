package es.networkersmc.authplugin.docs;

import es.networkersmc.dendera.database.annotation.DatabaseName;
import es.networkersmc.dendera.database.annotation.ModelName;
import es.networkersmc.dendera.document.SimpleEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@DatabaseName("denderadb")
@ModelName("authenticationdata")
public class AuthenticationData extends SimpleEntity {

    @Getter @Setter private String password;

    public AuthenticationData() {
        // Constructor for Jackson
    }

    public AuthenticationData(UUID uuid) {
        super(uuid);
    }
}
