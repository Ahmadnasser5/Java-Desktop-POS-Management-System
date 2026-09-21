package ui.components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ui.theme.ThemeManager;

/**
 * SHARED COMPONENT - styled yes/no modal.
 * <p>
 * Built from project components instead of {@code javafx.scene.control.Alert},
 * because Alert renders with the platform look and would break the single
 * visual system required by the UI contract.
 *
 * <pre>{@code
 * if (ConfirmDialog.confirmDanger(owner, "Delete user",
 *         "This cannot be undone.", "Delete")) { ... }
 * }</pre>
 */
public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    public static boolean confirm(Window owner, String title, String message, String confirmText) {
        return show(owner, title, message, confirmText, AppButton.Variant.PRIMARY);
    }

    public static boolean confirmDanger(Window owner, String title, String message,
                                        String confirmText) {
        return show(owner, title, message, confirmText, AppButton.Variant.DANGER);
    }

    private static boolean show(Window owner, String title, String message,
                                String confirmText, AppButton.Variant variant) {

        final boolean[] result = {false};

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle(title);
        stage.setResizable(false);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("text-normal");
        messageLabel.setWrapText(true);

        AppButton cancelButton = new AppButton("Cancel", AppButton.Variant.SECONDARY);
        cancelButton.setOnAction(event -> stage.close());

        AppButton confirmButton = new AppButton(confirmText, variant);
        confirmButton.setOnAction(event -> {
            result[0] = true;
            stage.close();
        });

        HBox actions = new HBox(cancelButton, confirmButton);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(new VBox(12, titleLabel, messageLabel), actions);
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(420);

        Scene scene = new Scene(root);
        ThemeManager.apply(scene);

        stage.setScene(scene);
        stage.showAndWait();

        return result[0];
    }
}
