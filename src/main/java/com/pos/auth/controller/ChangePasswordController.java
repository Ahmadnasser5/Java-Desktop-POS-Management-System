package com.pos.auth.controller;

import com.pos.auth.service.AuthService;
import com.pos.core.util.AlertUtil;
import com.pos.core.util.Navigator;
import com.pos.core.util.ValidationUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ui.components.AppButton;
import ui.components.AppPasswordField;
import ui.theme.ThemeManager;

import java.io.IOException;

/**
 * "Change my own password" dialog.
 * <p>
 * Used in two situations: voluntarily, and forced at first login when
 * {@code users.must_change_password} is set.
 */
public class ChangePasswordController {

    private static final String VIEW = "/fxml/users/change-password-dialog.fxml";

    @FXML private Label dialogTitleLabel;
    @FXML private Label forcedNoticeLabel;
    @FXML private AppPasswordField currentPasswordField;
    @FXML private AppPasswordField newPasswordField;
    @FXML private AppPasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private AppButton cancelButton;

    private final AuthService authService = new AuthService();

    private Stage stage;
    private boolean forced;
    private boolean changed;

    @FXML
    private void initialize() {
        hideError();
        setForced(false);
    }

    /**
     * Opens the dialog in forced mode. The user cannot dismiss it without
     * setting a new password.
     *
     * @return true when the password was changed
     */
    public static boolean showForced(Window owner) {
        return open(owner, true);
    }

    public static boolean show(Window owner) {
        return open(owner, false);
    }

    private static boolean open(Window owner, boolean forced) {
        try {
            FXMLLoader loader = Navigator.loader(VIEW);
            Parent root = loader.load();

            ChangePasswordController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setTitle(forced ? "تعيين كلمة مرور جديدة" : "تغيير كلمة المرور");
            stage.setResizable(false);

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            stage.setScene(scene);

            controller.stage = stage;
            controller.setForced(forced);

            stage.showAndWait();
            return controller.changed;

        } catch (IOException e) {
            throw new IllegalStateException("Could not open the change password dialog", e);
        }
    }

    private void setForced(boolean forced) {
        this.forced = forced;
        dialogTitleLabel.setText(forced ? "تعيين كلمة مرور جديدة" : "تغيير كلمة المرور");
        forcedNoticeLabel.setVisible(forced);
        forcedNoticeLabel.setManaged(forced);
        cancelButton.setText(forced ? "تسجيل الخروج" : "إلغاء");
        if (stage != null) {
            stage.setOnCloseRequest(event -> {
                if (forced && !changed) {
                    event.consume();
                }
            });
        }
    }

    @FXML
    private void onSave() {
        hideError();

        char[] current = toChars(currentPasswordField.getText());
        char[] fresh = toChars(newPasswordField.getText());
        char[] confirm = toChars(confirmPasswordField.getText());

        try {
            if (current.length == 0) {
                showError("من فضلك أدخل كلمة المرور الحالية.");
                return;
            }
            if (!java.util.Arrays.equals(fresh, confirm)) {
                showError("كلمتا المرور الجديدتان غير متطابقتين.");
                return;
            }

            authService.changeOwnPassword(current, fresh);
            changed = true;
            close();

        } catch (RuntimeException e) {
            showError(AlertUtil.userMessage(e));
        } finally {
            ValidationUtil.wipe(current);
            ValidationUtil.wipe(fresh);
            ValidationUtil.wipe(confirm);
        }
    }

    @FXML
    private void onCancel() {
        changed = false;
        close();
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }

    private static char[] toChars(String value) {
        return value == null ? new char[0] : value.toCharArray();
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
    }
}
