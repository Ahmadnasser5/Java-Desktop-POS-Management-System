package ui.layout;

import com.pos.auth.security.Permissions;
import com.pos.auth.service.AuthorizationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * SHARED COMPONENT - primary navigation.
 * <p>
 * Every item is bound to a permission code from the Authentication module.
 * Items the current role cannot use are hidden, which satisfies the UI
 * Definition of Done: "User permissions correctly hide or disable restricted
 * navigation nodes". This is the second shared file whose change was declared
 * in the Step 1 plan.
 * <p>
 * Each engineer replaces the placeholder call in their own handler with their
 * real view path - nothing else in this file needs to change.
 */
public class SidebarController {

    @FXML private Button dashboardNavButton;
    @FXML private Button posNavButton;
    @FXML private Button productsNavButton;
    @FXML private Button categoriesNavButton;
    @FXML private Button usersNavButton;
    @FXML private Button reportsNavButton;

    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
        applyPermissions();
        openDefaultViewForRole();
    }

    /** Hides navigation the current role has no permission for. */
    public void applyPermissions() {
        bind(dashboardNavButton, AuthorizationService.hasPermission(Permissions.DASHBOARD_VIEW));
        bind(posNavButton, AuthorizationService.hasPermission(Permissions.SALE_CREATE));
        bind(productsNavButton, AuthorizationService.hasPermission(Permissions.PRODUCT_VIEW));
        bind(categoriesNavButton, AuthorizationService.hasPermission(Permissions.PRODUCT_MANAGE));
        bind(usersNavButton, AuthorizationService.hasPermission(Permissions.USER_VIEW));
        bind(reportsNavButton, AuthorizationService.hasPermission(Permissions.REPORT_VIEW));
    }

    private void bind(Button button, boolean allowed) {
        button.setVisible(allowed);
        button.setManaged(allowed);
    }

    /** ADMIN lands on the dashboard area, SALES lands on the POS area. */
    private void openDefaultViewForRole() {
        if (AuthorizationService.isAdmin()) {
            onDashboard();
        } else if (AuthorizationService.isSales()) {
            onPos();
        }
    }

    private void select(Button active) {
        for (Button button : new Button[]{dashboardNavButton, posNavButton, productsNavButton,
                categoriesNavButton, usersNavButton, reportsNavButton}) {
            button.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    // --------------------------------------------------------------- actions
    // Engineer 1 owns only onUsers(). The others are integration stubs that
    // the owning engineer replaces with mainLayout.loadView("/fxml/...").

    @FXML
    private void onDashboard() {
        select(dashboardNavButton);
        mainLayout.showModulePlaceholder("لوحة التحكم",
                "هذه الوحدة من مسؤولية المهندس رقم 5 ولم يتم دمجها بعد.");
    }

    @FXML
    private void onPos() {
        select(posNavButton);
        mainLayout.showModulePlaceholder("نقطة البيع",
                "هذه الوحدة من مسؤولية المهندس رقم 3 ولم يتم دمجها بعد.");
    }

    @FXML
    private void onProducts() {
        select(productsNavButton);
        mainLayout.showModulePlaceholder("المنتجات والمخزون",
                "هذه الوحدة من مسؤولية المهندس رقم 2 ولم يتم دمجها بعد.");
    }

    @FXML
    private void onCategories() {
        select(categoriesNavButton);
        mainLayout.showModulePlaceholder("التصنيفات",
                "هذه الوحدة من مسؤولية المهندس رقم 2 ولم يتم دمجها بعد.");
    }

    @FXML
    private void onUsers() {
        select(usersNavButton);
        mainLayout.loadView("/fxml/users/users-view.fxml");
    }

    @FXML
    private void onReports() {
        select(reportsNavButton);
        mainLayout.showModulePlaceholder("التقارير",
                "هذه الوحدة من مسؤولية المهندس رقم 5 ولم يتم دمجها بعد.");
    }
}
