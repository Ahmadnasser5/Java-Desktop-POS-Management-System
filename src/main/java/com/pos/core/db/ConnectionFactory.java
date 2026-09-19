package com.pos.core.db;

import com.pos.core.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The single place in the whole project that opens a JDBC connection.
 * <p>
 * SHARED COMPONENT - Engineers 2..5 must use this instead of calling
 * {@link DriverManager} themselves.
 *
 * <pre>{@code
 * try (Connection conn = ConnectionFactory.getConnection();
 *      PreparedStatement ps = conn.prepareStatement(SQL)) {
 *     ...
 * } catch (SQLException e) {
 *     throw new DataAccessException("...", e);
 * }
 * }</pre>
 *
 * <p>Design note: one short-lived connection per operation, closed by
 * try-with-resources. A connection pool (HikariCP) was deliberately left out -
 * a single-user desktop client does not keep connections idle long enough for
 * MySQL {@code wait_timeout} to become a problem, and every extra dependency
 * is a cost for a five-person student team. If we later add a background
 * refresh thread or multi-terminal deployment, swapping the body of
 * {@link #getConnection()} for a pooled DataSource is a one-class change.
 */
public final class ConnectionFactory {

    private ConnectionFactory() {
    }

    public static Connection getConnection() {
        DatabaseConfig config = DatabaseConfig.get();
        try {
            return DriverManager.getConnection(
                    config.getUrl(), config.getUser(), config.getPassword());
        } catch (SQLException e) {
            // The message is intentionally generic: it may surface in a log file.
            throw new DataAccessException("تعذّر الاتصال بقاعدة البيانات.", e);
        }
    }

    /**
     * Fails fast at application start-up so the user gets one clear message
     * instead of a broken login screen.
     *
     * @return true when a connection could be opened and closed again
     */
    public static boolean testConnection() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException | DataAccessException e) {
            return false;
        }
    }
}
