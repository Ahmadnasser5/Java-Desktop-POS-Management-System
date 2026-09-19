package com.pos.auth.exception;

/**
 * Thrown when user supplied data fails a business rule (empty username,
 * duplicate username, password too short...). The message is meant to be
 * displayed directly in the form.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
