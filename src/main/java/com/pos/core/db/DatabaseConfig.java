package com.pos.core.db;

import com.pos.core.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the database configuration once, at startup.
 * <p>
 * Resolution order (first non-blank wins):
 * <ol>
 *   <li>Environment variables {@code POS_DB_URL}, {@code POS_DB_USER},
 *       {@code POS_DB_PASSWORD}</li>
 *   <li>{@code db.properties} on the classpath</li>
 * </ol>
 * No credential is ever hard-coded here, and {@code db.properties} is
 * git-ignored. {@code db.properties.example} is the file that is committed.
 */
public final class DatabaseConfig {

    private static final String RESOURCE = "/db.properties";

    private static DatabaseConfig instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static synchronized DatabaseConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static DatabaseConfig load() {
        Properties props = new Properties();

        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new DataAccessException("تعذّر قراءة ملف db.properties", e);
        }

        String url = resolve("POS_DB_URL", props, "db.url");
        String user = resolve("POS_DB_USER", props, "db.user");
        String password = resolve("POS_DB_PASSWORD", props, "db.password");

        if (isBlank(url) || isBlank(user)) {
            throw new DataAccessException(
                    "إعدادات قاعدة البيانات غير موجودة. من فضلك انسخ ملف "
                            + "src/main/resources/db.properties.example إلى db.properties "
                            + "واملأه، أو اضبط المتغيرات POS_DB_URL / POS_DB_USER / POS_DB_PASSWORD.");
        }
        return new DatabaseConfig(url, user, password == null ? "" : password);
    }

    private static String resolve(String envKey, Properties props, String propKey) {
        String fromEnv = System.getenv(envKey);
        if (!isBlank(fromEnv)) {
            return fromEnv.trim();
        }
        String fromFile = props.getProperty(propKey);
        return fromFile == null ? null : fromFile.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    /** Never log or display the returned value. */
    public String getPassword() {
        return password;
    }
}
