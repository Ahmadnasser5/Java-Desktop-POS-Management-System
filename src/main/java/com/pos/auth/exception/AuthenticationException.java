package com.pos.auth.exception;

/**
 * Thrown when a login attempt fails.
 * <p>
 * The message is always safe to show to the user: it never contains SQL,
 * stack traces, or information that would let an attacker tell "unknown
 * username" apart from "wrong password".
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
