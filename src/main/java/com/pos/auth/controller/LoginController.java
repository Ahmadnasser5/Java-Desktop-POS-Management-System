package com.pos.auth.controller;

import com.pos.auth.model.User;
import com.pos.auth.service.AuthService;
import com.pos.core.db.ConnectionFactory;
import com.pos.core.util.AlertUtil;
import com.pos.core.util.Navigator;
import com.pos.core.util.ValidationUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import ui.components.AppButton;
import ui.components.AppPasswordField;
import ui.components.AppTextField;

/**
 * Pre-authentication screen.
 * <p>
 * Contains no SQL and no hashing - it collects two fields, hands them to
 * {@link AuthService}, and reacts to the result. The database call runs on a
 * background {@link Task} so BCrypt verification (~250ms) never freezes the UI.
 */
public class LoginController {

    @FXML private AppTextField usernameField;
    @FXML private AppPasswordField passwordField;
    @FXML private AppButton loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loginProgressIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        hideError();
        loginProgressIndicator.setVisible(false);
        loginProgressIndicator.setManaged(false);

        // Enter submits from either field.
        usernameField.setOnAction(event -> onLogin());
        passwordField.setOnAction(event -> onLogin());

        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void onLogin() {
        hideError();

        final String username = usernameField.getText();
        final char[] password = passwordField.getText() == null
                ? new char[0]
                : passwordField.getText().toCharArray();

        if (ValidationUtil.isBlank(username)) {
            showError("من فضلك أدخل اسم المستخدم.");
            usernameField.setInvalid(true);
            usernameField.requestFocus();
            return;
        }
        if (password.length == 0) {
            showError("من فضلك أدخل كلمة المرور.");
            passwordField.setInvalid(true);
            passwordField.requestFocus();
            return;
        }

        setBusy(true);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            ValidationUtil.wipe(password);
            passwordField.clear();
            setBusy(false);
            onLoginSuccess(loginTask.getValue());
        });

        loginTask.setOnFailed(event -> {
            ValidationUtil.wipe(password);
            passwordField.clear();
            setBusy(false);
            showError(AlertUtil.userMessage(loginTask.getException()));
            passwordField.requestFocus();
        });

        new Thread(loginTask, "login-task").start();
    }

    private void onLoginSuccess(User user) {
        if (user.isMustChangePassword()) {
            boolean changed = ChangePasswordController.showForced(
                    loginButton.getScene().getWindow());
            if (!changed) {
                // The user refused to set a new password - do not let them in.
                authService.logout();
                showError("يجب تعيين كلمة مرور جديدة قبل تسجيل الدخول.");
                return;
            }
        }
        Navigator.showMainApplication();
    }

    /** Called once at start-up so a broken database is reported clearly. */
    public void warnIfDatabaseUnreachable() {
        if (!ConnectionFactory.testConnection()) {
            showError("تعذّر الاتصال بقاعدة البيانات. تأكد من تشغيل MySQL "
                    + "ومن ضبط ملف db.properties.");
            loginButton.setDisable(true);
        }
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        loginProgressIndicator.setVisible(busy);
        loginProgressIndicator.setManaged(busy);
        loginButton.setText(busy ? "جارٍ تسجيل الدخول..." : "تسجيل الدخول");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        usernameField.setInvalid(false);
        passwordField.setInvalid(false);
    }
}
