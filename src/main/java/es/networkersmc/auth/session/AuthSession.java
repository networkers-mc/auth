package es.networkersmc.auth.session;

import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.dendera.docs.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSession {

    private final User user;
    private AuthState state;
    private final AuthenticationData data;

    private char[] buffer; // For REGISTER_CONFIRM and CHANGE_PASSWORD_CONFIRM states

    public AuthSession(User user, AuthState state, AuthenticationData data) {
        this.user = user;
        this.state = state;
        this.data = data;
    }

}
