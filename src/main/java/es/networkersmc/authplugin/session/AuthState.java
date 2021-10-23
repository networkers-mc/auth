package es.networkersmc.authplugin.session;

public enum AuthState {

    REGISTER,
    REGISTER_CONFIRM,
    LOGIN,
    CHANGE_PASSWORD,
    CHANGE_PASSWORD_CONFIRM,

    /**
     * When a player is logged in and waiting to be sent to a HUB node.
     */
    LOGGED_IN;

}
