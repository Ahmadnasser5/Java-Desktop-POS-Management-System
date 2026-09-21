package com.pos.auth.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * BCrypt implementation backed by {@code at.favre.lib:bcrypt}.
 * <p>
 * The salt is generated per password and stored inside the resulting hash
 * string, so the database needs no separate salt column.
 * <p>
 * Cost factor 12 - roughly 250ms per verification on a typical laptop, which
 * is slow enough to make offline brute force expensive and fast enough that a
 * cashier does not notice it at login.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int COST = 12;

    @Override
    public String hash(char[] plainPassword) {
        if (plainPassword == null || plainPassword.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.withDefaults().hashToString(COST, plainPassword);
    }

    @Override
    public boolean verify(char[] plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.length == 0
                || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(plainPassword, storedHash.toCharArray()).verified;
        } catch (IllegalArgumentException e) {
            // Malformed hash in the database - treat as a failed login, never crash.
            return false;
        }
    }
}
