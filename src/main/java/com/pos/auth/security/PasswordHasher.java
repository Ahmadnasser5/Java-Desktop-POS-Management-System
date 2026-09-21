package com.pos.auth.security;

/**
 * Abstraction over the password hashing algorithm.
 * <p>
 * Exists so the algorithm can be replaced (BCrypt -> Argon2id) by writing one
 * new class, without touching services, DAOs or controllers.
 */
public interface PasswordHasher {

    /**
     * @param plainPassword the raw password; the caller must discard it right after
     * @return an encoded hash, safe to store in {@code users.password_hash}
     */
    String hash(char[] plainPassword);

    /**
     * Constant-time comparison of a candidate password against a stored hash.
     *
     * @return true only when the password matches
     */
    boolean verify(char[] plainPassword, String storedHash);
}
