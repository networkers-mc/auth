package es.networkersmc.auth.session;

import es.networkersmc.auth.docs.AuthenticationData;
import es.networkersmc.dendera.docs.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSession {

    private final User user;
    private final AuthenticationData data;

    private AuthState state;
    private char[] buffer; // For REGISTER_CONFIRM and CHANGE_PASSWORD_CONFIRM states

    public AuthSession(User user, AuthenticationData data, AuthState state) {
        this.user = user;
        this.data = data;
        this.state = state;
    }
}
