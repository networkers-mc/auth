package es.networkersmc.auth.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PasswordRequirements {

    public boolean isValid(String password) {
        return len(password) && chars(password);
    }

    private boolean len(String password) {
        return password.length() > 4 && password.length() <= 64;
    }

    private boolean chars(String password) {
        for (char c : password.toCharArray()) {
            if (notBetween(c, (byte) 0x21, (byte) 0x7e)         // ! -> ~
                    && notBetween(c, (byte) 0xa1, (byte) 0xa6)  // ¡ -> |
                    && notBetween(c, (byte) 0xbf, (byte) 0xff)  // ¿ -> ÿ
            ) {
                return false;
            }
        }
        return true;
    }

    private boolean notBetween(char c, byte min, byte max) {
        return c < min || c > max;
    }

}
