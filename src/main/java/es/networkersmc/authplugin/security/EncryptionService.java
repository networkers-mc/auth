package es.networkersmc.authplugin.security;

public interface EncryptionService {

    /**
     * Hashes the given password.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    String hash(String password);

    /**
     * Verifies if a password corresponds to the given hash.
     *
     * @param password the password to verify
     * @param hash     the hashed password
     * @return if the passwords corresponds to the hash
     */
    boolean verify(String password, String hash);

}
