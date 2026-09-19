package com.pos.core.exception;

/**
 * Wraps every low level persistence failure (SQLException, missing driver,
 * unreachable database...).
 * <p>
 * DAOs must never let a {@code java.sql.SQLException} escape: the service and
 * controller layers are not allowed to know that the storage engine is JDBC,
 * and raw SQL messages must never reach the user interface.
 * <p>
 * Shared class - Engineers 2..5 should reuse it in their own DAOs.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
