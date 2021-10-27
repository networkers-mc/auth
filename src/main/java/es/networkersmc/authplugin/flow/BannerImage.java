package es.networkersmc.authplugin.flow;

public enum BannerImage {

    LOGIN,
    LOGIN_WRONG_PASSWORD,

    REGISTER,
    REGISTER_PASSWORDS_DONT_MATCH,

    CHANGE_PASSWORD,
    CHANGE_PASSWORD_PASSWORDS_DONT_MATCH,

    CONFIRM_PASSWORD; // For both REGISTER and CHANGE_PASSWORD

}
