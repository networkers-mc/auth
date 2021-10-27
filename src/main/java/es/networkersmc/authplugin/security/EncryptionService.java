package es.networkersmc.authplugin.security;

public interface EncryptionService {

    /**
     * Hashes the given password.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    char[] hash(char[] password);

    /**
     * Hashes the given password.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    default String hash(String password) {
        return new String(this.hash(password.toCharArray()));
    }

    /**
     * Verifies if a password corresponds to the given hash.
     *
     * @param password the password to verify
     * @param hash     the hashed password
     * @return if the passwords corresponds to the hash
     */
    boolean verify(char[] password, char[] hash);

    /**
     * Verifies if a password corresponds to the given hash.
     *
     * @param password the password to verify
     * @param hash     the hashed password
     * @return if the passwords corresponds to the hash
     */
    default boolean verify(String password, String hash) {
        return this.verify(password.toCharArray(), hash.toCharArray());
    }

}
