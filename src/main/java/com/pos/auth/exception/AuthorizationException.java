package com.pos.auth.exception;

/**
 * Thrown when the logged-in user tries to perform an action their role does
 * not allow. Always raised from the SERVICE layer - hiding a button in the UI
 * is user experience, not security.
 */
public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }
}
