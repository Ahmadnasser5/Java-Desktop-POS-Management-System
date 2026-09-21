package com.pos.auth.controller;

import com.pos.auth.model.User;
import com.pos.auth.security.Permissions;
import com.pos.auth.security.SessionManager;
import com.pos.auth.service.AuthorizationService;
import com.pos.auth.service.UserService;
import com.pos.core.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ui.components.AppButton;
import ui.components.AppTableView;
import ui.components.AppTextField;
import ui.components.ConfirmDialog;
import ui.components.LoadingOverlay;
import ui.components.PlaceholderNode;
import ui.components.Toast;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User Management screen. Lives inside the shared Content Area - it creates no
 * top bar, no sidebar, no status bar and no window of its own.
 */
public class UserListController {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private StackPane usersRootPane;
    @FXML private AppTextField userSearchField;
    @FXML private AppButton addUserButton;
    @FXML private AppTableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, User> statusColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;
    @FXML private TableColumn<User, User> actionsColumn;
    @FXML private LoadingOverlay usersLoadingOverlay;
    @FXML private Label usersCountLabel;

    private final UserService userService = new UserService();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureColumns();

        usersTable.setItems(users);
        usersTable.setPlaceholder(new PlaceholderNode("لا يوجد مستخدمون",
                "جرّب كلمة بحث مختلفة، أو أضف مستخدماً جديداً."));

        addUserButton.setVisible(AuthorizationService.hasPermission(Permissions.USER_MANAGE));
        addUserButton.setManaged(addUserButton.isVisible());

        userSearchField.textProperty().addListener((obs, oldValue, newValue) -> reload(newValue));

        reload(null);
    }

    private void configureColumns() {
        usernameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername()));

        fullNameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFullName() == null ? "-" : data.getValue().getFullName()));

        roleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        com.pos.core.util.Labels.roleName(data.getValue().getRoleName())));

        lastLoginColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLastLoginAt() == null
                        ? "لم يسجّل الدخول من قبل"
                        : data.getValue().getLastLoginAt().format(DATE_TIME)));

        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(com.pos.core.util.Labels.activeStatus(user.isActive()));
                badge.getStyleClass().addAll("status-badge",
                        user.isActive() ? "status-badge-active" : "status-badge-inactive");
                setGraphic(badge);
            }
        });

        actionsColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null
                        || !AuthorizationService.hasPermission(Permissions.USER_MANAGE)) {
                    setGraphic(null);
                    return;
                }
                setGraphic(buildRowActions(user));
            }
        });
    }

    private HBox buildRowActions(User user) {
        AppButton editButton = new AppButton("تعديل", AppButton.Variant.SECONDARY);
        editButton.getStyleClass().add("table-action-button");
        editButton.setOnAction(event -> onEdit(user));

        AppButton resetButton = new AppButton("إعادة تعيين كلمة المرور", AppButton.Variant.SECONDARY);
        resetButton.getStyleClass().add("table-action-button");
        resetButton.setOnAction(event -> onResetPassword(user));

        boolean isSelf = user.getId() == SessionManager.getInstance().getCurrentUserId();

        AppButton toggleButton = new AppButton(
                user.isActive() ? "إيقاف" : "تفعيل",
                user.isActive() ? AppButton.Variant.DANGER : AppButton.Variant.SUCCESS);
        toggleButton.getStyleClass().add("table-action-button");
        toggleButton.setDisable(isSelf);
        toggleButton.setOnAction(event -> onToggleActive(user));

        HBox box = new HBox(8, editButton, resetButton, toggleButton);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ------------------------------------------------------------------ load

    private void reload(String searchTerm) {
        usersLoadingOverlay.show("جارٍ تحميل المستخدمين...");

        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return userService.search(searchTerm);
            }
        };

        task.setOnSucceeded(event -> {
            users.setAll(task.getValue());
            usersCountLabel.setText(users.size() + " مستخدم");
            usersLoadingOverlay.hide();
        });

        task.setOnFailed(event -> {
            usersLoadingOverlay.hide();
            users.clear();
            usersCountLabel.setText("");
            usersTable.setPlaceholder(new PlaceholderNode("تعذّر تحميل المستخدمين",
                    AlertUtil.userMessage(task.getException())));
        });

        new Thread(task, "users-load-task").start();
    }

    // --------------------------------------------------------------- actions

    @FXML
    private void onAddUser() {
        boolean saved = UserFormController.openForCreate(window());
        if (saved) {
            Toast.success(usersRootPane, "تم إنشاء المستخدم.");
            reload(userSearchField.getText());
        }
    }

    private void onEdit(User user) {
        boolean saved = UserFormController.openForEdit(window(), user);
        if (saved) {
            Toast.success(usersRootPane, "تم حفظ التعديلات.");
            reload(userSearchField.getText());
        }
    }

    private void onResetPassword(User user) {
        boolean saved = UserFormController.openForPasswordReset(window(), user);
        if (saved) {
            Toast.success(usersRootPane,
                    "تم إعادة تعيين كلمة المرور. سيُطلب من المستخدم تغييرها عند تسجيل الدخول القادم.");
        }
    }

    private void onToggleActive(User user) {
        boolean activating = !user.isActive();

        boolean confirmed = activating
                ? ConfirmDialog.confirm(window(), "تفعيل الحساب",
                        "السماح لـ " + user.getUsername() + " بتسجيل الدخول مرة أخرى؟", "تفعيل")
                : ConfirmDialog.confirmDanger(window(), "إيقاف الحساب",
                        "لن يتمكن " + user.getUsername() + " من تسجيل الدخول بعد الآن. "
                                + "سيتم الاحتفاظ بسجل مبيعاته.", "إيقاف");

        if (!confirmed) {
            return;
        }

        try {
            userService.setActive(user.getId(), activating);
            Toast.success(usersRootPane, activating ? "تم تفعيل الحساب." : "تم إيقاف الحساب.");
            reload(userSearchField.getText());
        } catch (RuntimeException e) {
            Toast.error(usersRootPane, AlertUtil.userMessage(e));
        }
    }

    private javafx.stage.Window window() {
        return usersRootPane.getScene() == null ? null : usersRootPane.getScene().getWindow();
    }
}
