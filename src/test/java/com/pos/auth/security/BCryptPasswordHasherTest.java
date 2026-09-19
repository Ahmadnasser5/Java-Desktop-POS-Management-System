package com.pos.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashIsNeverThePlaintext() {
        String hash = hasher.hash("Secret123".toCharArray());
        assertNotEquals("Secret123", hash);
        assertTrue(hash.startsWith("$2"));
        assertEquals(60, hash.length());
    }

    @Test
    void verifyAcceptsTheCorrectPassword() {
        String hash = hasher.hash("Secret123".toCharArray());
        assertTrue(hasher.verify("Secret123".toCharArray(), hash));
    }

    @Test
    void verifyRejectsAWrongPassword() {
        String hash = hasher.hash("Secret123".toCharArray());
        assertFalse(hasher.verify("secret123".toCharArray(), hash));
        assertFalse(hasher.verify("Secret1234".toCharArray(), hash));
    }

    @Test
    void samePasswordProducesDifferentHashes() {
        String first = hasher.hash("Secret123".toCharArray());
        String second = hasher.hash("Secret123".toCharArray());
        assertNotEquals(first, second, "each hash must use a fresh salt");
        assertTrue(hasher.verify("Secret123".toCharArray(), first));
        assertTrue(hasher.verify("Secret123".toCharArray(), second));
    }

    @Test
    void verifyIsSafeWithBadInput() {
        assertFalse(hasher.verify(null, "$2a$12$abc"));
        assertFalse(hasher.verify("x".toCharArray(), null));
        assertFalse(hasher.verify("x".toCharArray(), "not-a-bcrypt-hash"));
    }

    @Test
    void emptyPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(new char[0]));
    }
}
