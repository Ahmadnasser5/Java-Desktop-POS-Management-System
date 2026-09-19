package ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * SHARED COMPONENT - semi transparent overlay shown while a background task
 * runs. Place it as the last child of a StackPane so it covers the content.
 *
 * <pre>{@code
 * loadingOverlay.show("Loading users...");
 * ... Task ...
 * loadingOverlay.hide();
 * }</pre>
 */
public class LoadingOverlay extends StackPane {

    private final Label messageLabel = new Label();

    public LoadingOverlay() {
        getStyleClass().add("loading-overlay");

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(48, 48);

        messageLabel.getStyleClass().add("text-secondary");

        VBox box = new VBox(12, indicator, messageLabel);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().add(box);
        hide();
    }

    public void show(String message) {
        messageLabel.setText(message == null ? "" : message);
        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }
}
