package com.pos.core.util;

import javafx.scene.control.Alert;
import javafx.stage.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Turns an exception into something safe to show a cashier.
 * <p>
 * Raw SQL messages and stack traces go to the log, never to the screen.
 */
public final class AlertUtil {

    private static final Logger LOG = Logger.getLogger(AlertUtil.class.getName());

    private AlertUtil() {
    }

    /**
     * @return a user-facing message. Known business exceptions keep their own
     *         wording; anything unexpected becomes a generic sentence.
     */
    public static String userMessage(Throwable error) {
        if (error == null) {
            return "حدث خطأ غير متوقع.";
        }
        if (error instanceof com.pos.auth.exception.ValidationException
                || error instanceof com.pos.auth.exception.AuthenticationException
                || error instanceof com.pos.auth.exception.AuthorizationException) {
            return error.getMessage();
        }
        if (error instanceof com.pos.core.exception.DataAccessException) {
            LOG.log(Level.SEVERE, "Data access failure", error);
            return error.getMessage() != null ? error.getMessage()
                    : "قاعدة البيانات غير متاحة حالياً. من فضلك حاول مرة أخرى.";
        }
        LOG.log(Level.SEVERE, "Unexpected failure", error);
        return "حدث خطأ غير متوقع. من فضلك حاول مرة أخرى أو تواصل مع الدعم الفني.";
    }

    /** Blocking error dialog, for start-up failures only. */
    public static void showFatal(Window owner, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("نظام نقاط البيع");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
