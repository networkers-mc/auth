package es.networkersmc.authplugin.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptEncryptionService implements EncryptionService {

    private static final int COST = 12; // 12 is recommended for these times

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();

    @Override
    public String hash(String password) {
        return hasher.hashToString(COST, password.toCharArray());
    }

    @Override
    public boolean verify(String password, String hash) {
        return verifyer.verify(password.toCharArray(), hash.toCharArray()).verified;
    }
}
