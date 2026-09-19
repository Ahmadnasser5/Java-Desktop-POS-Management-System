package ui.layout;

import com.pos.auth.security.SessionManager;
import com.pos.auth.service.AuthService;
import com.pos.core.util.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.components.ConfirmDialog;

/**
 * SHARED COMPONENT - branding on the left, current user and logout on the right.
 * <p>
 * The user name, role badge and logout action are fed by the Authentication
 * module (Engineer 1) through {@link SessionManager}. This is the shared file
 * whose change was declared in the Step 1 plan.
 */
public class TopBarController {

    @FXML private Label appNameLabel;
    @FXML private Label currentUsernameLabel;
    @FXML private Label currentRoleLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        appNameLabel.setText("نظام نقاط البيع");
        refreshUser();
    }

    /** Called after login, and after the user edits their own profile. */
    public void refreshUser() {
        SessionManager session = SessionManager.getInstance();
        currentUsernameLabel.setText(
                session.getCurrentUsername() == null ? "-" : session.getCurrentUsername());
        currentRoleLabel.setText(
                session.getCurrentRoleName() == null
                        ? "-"
                        : com.pos.core.util.Labels.roleName(session.getCurrentRoleName()));
    }

    @FXML
    private void onLogout() {
        boolean confirmed = ConfirmDialog.confirm(
                appNameLabel.getScene().getWindow(),
                "تسجيل الخروج",
                "هل تريد تسجيل الخروج من النظام؟",
                "تسجيل الخروج");

        if (confirmed) {
            authService.logout();
            Navigator.showLogin();
        }
    }
}
