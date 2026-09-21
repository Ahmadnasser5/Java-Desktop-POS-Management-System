package ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * SHARED COMPONENT - the empty / error state required by the UI Definition of
 * Done. Used as a TableView placeholder or dropped into any container.
 */
public class PlaceholderNode extends VBox {

    private final Label titleLabel = new Label();
    private final Label detailLabel = new Label();

    public PlaceholderNode() {
        this("لا يوجد ما يُعرض", null);
    }

    public PlaceholderNode(String title, String detail) {
        getStyleClass().add("placeholder-node");

        titleLabel.getStyleClass().add("placeholder-title");
        detailLabel.getStyleClass().add("placeholder-detail");
        detailLabel.setWrapText(true);

        setTitle(title);
        setDetail(detail);

        getChildren().addAll(titleLabel, detailLabel);
    }

    public void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
    }

    public void setDetail(String detail) {
        detailLabel.setText(detail == null ? "" : detail);
        detailLabel.setVisible(detail != null && !detail.isEmpty());
        detailLabel.setManaged(detail != null && !detail.isEmpty());
    }
}
