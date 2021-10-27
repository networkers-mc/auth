package es.networkersmc.authplugin.session;

public enum AuthState {

    REGISTER,
    REGISTER_CONFIRM,
    LOGIN,
    CHANGE_PASSWORD,         // Not implemented at the moment
    CHANGE_PASSWORD_CONFIRM, // Not implemented at the moment

    /**
     * When a player is logged in and waiting to be sent to a HUB node.
     */
    LOGGED_IN;

}
