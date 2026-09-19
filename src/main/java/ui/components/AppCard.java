package ui.components;

import javafx.scene.layout.VBox;

/**
 * SHARED COMPONENT - white surface container: 16px padding, 12px spacing,
 * 6px radius, 1px border.
 */
public class AppCard extends VBox {

    public AppCard() {
        getStyleClass().add("card");
    }
}
