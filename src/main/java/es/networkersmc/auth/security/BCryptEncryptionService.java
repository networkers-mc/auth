package es.networkersmc.auth.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptEncryptionService implements EncryptionService {

    private static final int COST = 12; // 12 is recommended for these times

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();

    @Override
    public char[] hash(char[] password) {
        return hasher.hashToChar(COST, password);
    }

    @Override
    public boolean verify(char[] password, char[] hash) {
        return verifyer.verify(password, hash).verified;
    }
}
