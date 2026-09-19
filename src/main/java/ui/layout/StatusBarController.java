package ui.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * SHARED COMPONENT - application state and version line.
 */
public class StatusBarController {

    private static final String VERSION = "الإصدار 1.0.0";

    @FXML private Label statusMessageLabel;
    @FXML private Label versionLabel;

    @FXML
    private void initialize() {
        versionLabel.setText(VERSION);
        setStatus("جاهز");
    }

    public void setStatus(String message) {
        statusMessageLabel.setText(message == null ? "" : message);
    }
}
