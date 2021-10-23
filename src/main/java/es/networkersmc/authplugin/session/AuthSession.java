package es.networkersmc.authplugin.session;

import es.networkersmc.authplugin.docs.AuthenticationData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSession {

    private AuthState state;
    private AuthenticationData data;

    private String buffer; // For REGISTER_CONFIRM and CHANGE_PASSWORD_CONFIRM states

    public AuthSession(AuthState state, AuthenticationData data) {
        this.state = state;
        this.data = data;
    }

}
