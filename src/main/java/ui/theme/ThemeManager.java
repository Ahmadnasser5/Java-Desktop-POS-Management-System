package ui.theme;

import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * SHARED COMPONENT - applies the project stylesheets and the application's
 * text direction.
 * <p>
 * Every Scene and every dialog root in the project must go through this class.
 * That is what guarantees one visual system across all five modules.
 * Order matters: variables.css first (colour tokens), then style.css.
 * <p>
 * The application is Arabic, right-to-left. Setting
 * {@link NodeOrientation#RIGHT_TO_LEFT} on the root mirrors every built-in
 * layout automatically (BorderPane left/right, HBox child order, ComboBox
 * arrow side, text alignment in TextField) - screens do not need to reverse
 * anything themselves.
 */
public final class ThemeManager {

    public static final String VARIABLES_CSS = "/css/variables.css";
    public static final String STYLE_CSS = "/css/style.css";

    private ThemeManager() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();
        scene.getStylesheets().addAll(url(VARIABLES_CSS), url(STYLE_CSS));
        scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    /** For dialog roots that are added to their own Scene. */
    public static void apply(Parent root) {
        if (root == null) {
            return;
        }
        root.getStylesheets().addAll(url(VARIABLES_CSS), url(STYLE_CSS));
        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    private static String url(String resource) {
        java.net.URL found = ThemeManager.class.getResource(resource);
        if (found == null) {
            throw new IllegalStateException("Missing stylesheet on the classpath: " + resource);
        }
        return found.toExternalForm();
    }
}
