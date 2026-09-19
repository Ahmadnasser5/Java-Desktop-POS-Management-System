package ui.components;

import javafx.scene.control.TextField;

/**
 * SHARED COMPONENT - standard text input (40px, 1px border, 6px radius).
 */
public class AppTextField extends TextField {

    public AppTextField() {
        getStyleClass().add("app-text-field");
    }

    public AppTextField(String text) {
        super(text);
        getStyleClass().add("app-text-field");
    }

    /** Adds or removes the red error border. */
    public void setInvalid(boolean invalid) {
        getStyleClass().remove("invalid");
        if (invalid) {
            getStyleClass().add("invalid");
        }
    }
}
