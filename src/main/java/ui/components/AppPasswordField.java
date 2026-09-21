package ui.components;

import javafx.scene.control.PasswordField;

/**
 * SHARED COMPONENT - password input, visually identical to AppTextField.
 * <p>
 * ADDITION to uiContract section 8: the contract lists AppTextField but the
 * contract also specifies PasswordField styling in section 7, and a login
 * screen cannot be built without one. Same style class, no new visuals.
 */
public class AppPasswordField extends PasswordField {

    public AppPasswordField() {
        getStyleClass().add("app-text-field");
    }

    public void setInvalid(boolean invalid) {
        getStyleClass().remove("invalid");
        if (invalid) {
            getStyleClass().add("invalid");
        }
    }
}
