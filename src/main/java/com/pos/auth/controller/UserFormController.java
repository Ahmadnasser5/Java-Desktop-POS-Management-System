package com.pos.auth.controller;

import com.pos.auth.model.Role;
import com.pos.auth.model.User;
import com.pos.auth.service.UserService;
import com.pos.core.util.AlertUtil;
import com.pos.core.util.Navigator;
import com.pos.core.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import ui.components.AppPasswordField;
import ui.components.AppTextField;
import ui.theme.ThemeManager;

import java.io.IOException;

/**
 * Add / edit user dialog, also used for an administrative password reset.
 * One FXML, three modes, so the three flows cannot drift apart visually.
 */
public class UserFormController {

    private static final String VIEW = "/fxml/users/user-form-dialog.fxml";

    private enum Mode { CREATE, EDIT, RESET_PASSWORD }

    @FXML private Label dialogTitleLabel;
    @FXML private VBox accountFieldsBox;
    @FXML private VBox passwordFieldsBox;
    @FXML private AppTextField usernameField;
    @FXML private AppTextField fullNameField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private AppPasswordField passwordField;
    @FXML private AppPasswordField confirmPasswordField;
    @FXML private Label passwordHintLabel;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    private Stage stage;
    private Mode mode = Mode.CREATE;
    private User editedUser;
    private boolean saved;

    // ------------------------------------------------------------- factories

    public static boolean openForCreate(Window owner) {
        return open(owner, Mode.CREATE, null);
    }

    public static boolean openForEdit(Window owner, User user) {
        return open(owner, Mode.EDIT, user);
    }

    public static boolean openForPasswordReset(Window owner, User user) {
        return open(owner, Mode.RESET_PASSWORD, user);
    }

    private static boolean open(Window owner, Mode mode, User user) {
        try {
            FXMLLoader loader = Navigator.loader(VIEW);
            Parent root = loader.load();

            UserFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setResizable(false);

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            stage.setScene(scene);

            controller.stage = stage;
            controller.applyMode(mode, user);

            stage.setTitle(controller.dialogTitleLabel.getText());
            stage.showAndWait();

            return controller.saved;

        } catch (IOException e) {
            throw new IllegalStateException("Could not open the user form", e);
        }
    }

    // ------------------------------------------------------------------ init

    @FXML
    private void initialize() {
        hideError();
        passwordHintLabel.setText(ValidationUtil.passwordPolicyMessage());

        // A converter alone sometimes fails to repaint the closed-state label
        // when the selection is made programmatically (applyMode() below does
        // exactly that in EDIT mode via getSelectionModel().select(role)).
        // Setting an explicit cellFactory + buttonCell forces JavaFX to call
        // updateItem() every time, whether the user clicked an item or the
        // code selected one - this is what actually fixes the blank label.
        Callback<ListView<Role>, ListCell<Role>> roleCellFactory = listView -> new ListCell<>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? "" : com.pos.core.util.Labels.roleName(role.getName()));
            }
        };

        roleComboBox.setCellFactory(roleCellFactory);
        roleComboBox.setButtonCell(roleCellFactory.call(null));
    }

    private void applyMode(Mode mode, User user) {
        this.mode = mode;
        this.editedUser = user;

        boolean showAccountFields = mode != Mode.RESET_PASSWORD;
        boolean showPasswordFields = mode != Mode.EDIT;

        accountFieldsBox.setVisible(showAccountFields);
        accountFieldsBox.setManaged(showAccountFields);
        passwordFieldsBox.setVisible(showPasswordFields);
        passwordFieldsBox.setManaged(showPasswordFields);

        if (showAccountFields) {
            try {
                roleComboBox.getItems().setAll(userService.findAllRoles());
            } catch (RuntimeException e) {
                showError(AlertUtil.userMessage(e));
            }
        }

        switch (mode) {
            case CREATE -> {
                dialogTitleLabel.setText("إضافة مستخدم");
                activeCheckBox.setSelected(true);
                if (!roleComboBox.getItems().isEmpty()) {
                    roleComboBox.getSelectionModel().selectFirst();
                }
            }
            case EDIT -> {
                dialogTitleLabel.setText("تعديل مستخدم");
                usernameField.setText(user.getUsername());
                fullNameField.setText(user.getFullName());
                activeCheckBox.setSelected(user.isActive());
                roleComboBox.getItems().stream()
                        .filter(role -> role.getId() == user.getRole().getId())
                        .findFirst()
                        .ifPresent(role -> roleComboBox.getSelectionModel().select(role));
            }
            case RESET_PASSWORD -> dialogTitleLabel.setText("إعادة تعيين كلمة المرور لـ " + user.getUsername());
        }
    }

    // --------------------------------------------------------------- actions

    @FXML
    private void onSave() {
        hideError();

        char[] password = toChars(passwordField.getText());
        char[] confirm = toChars(confirmPasswordField.getText());

        try {
            if (mode != Mode.EDIT && !java.util.Arrays.equals(password, confirm)) {
                showError("كلمتا المرور غير متطابقتين.");
                return;
            }

            switch (mode) {
                case CREATE -> {
                    Role role = roleComboBox.getValue();
                    if (role == null) {
                        showError("من فضلك اختر دوراً.");
                        return;
                    }
                    userService.createUser(usernameField.getText(), password,
                            fullNameField.getText(), role.getId(), activeCheckBox.isSelected());
                }
                case EDIT -> {
                    Role role = roleComboBox.getValue();
                    if (role == null) {
                        showError("من فضلك اختر دوراً.");
                        return;
                    }
                    userService.updateUser(editedUser.getId(), usernameField.getText(),
                            fullNameField.getText(), role.getId(), activeCheckBox.isSelected());
                }
                case RESET_PASSWORD -> userService.resetPassword(editedUser.getId(), password);
            }

            saved = true;
            stage.close();

        } catch (RuntimeException e) {
            showError(AlertUtil.userMessage(e));
        } finally {
            ValidationUtil.wipe(password);
            ValidationUtil.wipe(confirm);
        }
    }

    @FXML
    private void onCancel() {
        saved = false;
        stage.close();
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