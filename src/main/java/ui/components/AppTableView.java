package ui.components;

import javafx.scene.control.TableView;

/**
 * SHARED COMPONENT - standard data table: bold 40px header on the background
 * colour, 38px fixed rows, 13px body text.
 *
 * @param <S> the row model type
 */
public class AppTableView<S> extends TableView<S> {

    public AppTableView() {
        getStyleClass().add("app-table");
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
}
