package ui.layout;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ui.components.PlaceholderNode;

/**
 * SHARED COMPONENT - the standard application frame:
 * TOP BAR / SIDEBAR / CONTENT / STATUS BAR.
 * <p>
 * Feature developers never modify this class. They only ask it to display
 * their own FXML inside the Content Area:
 *
 * <pre>{@code
 * MainLayoutController.getInstance().loadView("/fxml/products/products-view.fxml");
 * }</pre>
 */
public class MainLayoutController {

    private static MainLayoutController instance;

    @FXML private StackPane contentArea;
    @FXML private StatusBarController statusBarController;
    @FXML private SidebarController sidebarController;
    @FXML private TopBarController topBarController;

    @FXML
    private void initialize() {
        instance = this;
        if (sidebarController != null) {
            sidebarController.setMainLayout(this);
        }
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    /**
     * Replaces the Content Area with the given view.
     *
     * @param fxmlResourcePath classpath path, e.g. "/fxml/users/users-view.fxml"
     */
    public void loadView(String fxmlResourcePath) {
        try {
            Node view = com.pos.core.util.Navigator.load(fxmlResourcePath);
            setContent(view);
        } catch (RuntimeException e) {
            setContent(new PlaceholderNode("تعذّر فتح هذه الشاشة",
                    com.pos.core.util.AlertUtil.userMessage(e)));
        }
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    /**
     * Temporary landing area for modules that are not merged yet, so the
     * navigation is testable end to end before the other four modules land.
     */
    public void showModulePlaceholder(String moduleName, String ownerNote) {
        setContent(new PlaceholderNode(moduleName, ownerNote));
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public StatusBarController getStatusBar() {
        return statusBarController;
    }

    public SidebarController getSidebar() {
        return sidebarController;
    }

    public TopBarController getTopBar() {
        return topBarController;
    }
}
