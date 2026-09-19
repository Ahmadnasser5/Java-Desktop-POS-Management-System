# Integration Contract — Authentication & Users (Engineer 1)

Everything in this file is stable. If any signature has to change, it will be
deprecated for one sprint and announced in the team channel first.

## 1. Who is logged in

```java
import com.pos.auth.security.SessionManager;

SessionManager session = SessionManager.getInstance();

session.isLoggedIn();          // boolean
session.getCurrentUser();      // User, or null
session.getCurrentUserId();    // int, or -1
session.getCurrentUsername();  // String, or null
session.getCurrentRoleName();  // "ADMIN" | "SALES" | null
session.getPermissions();      // unmodifiable Set<String>
```

The most common call in the project — Engineer 3, when creating a sale:

```java
sale.setUserId(SessionManager.getInstance().getCurrentUserId());
```

The session object never holds a password or a password hash.

## 2. Can they do this?

```java
import com.pos.auth.service.AuthorizationService;
import com.pos.auth.security.Permissions;

AuthorizationService.isAdmin();
AuthorizationService.isSales();
AuthorizationService.hasPermission(Permissions.SALE_CANCEL);       // boolean, for hiding UI
AuthorizationService.hasAnyPermission(Permissions.A, Permissions.B);
AuthorizationService.requirePermission(Permissions.SALE_CANCEL);   // throws AuthorizationException
```

Rule of thumb: `hasPermission` in the controller to hide a button,
`requirePermission` at the top of the service method to actually enforce it.
Hiding a button is UX, not security.

Available codes are in `com.pos.auth.security.Permissions`. Use the constants,
not string literals — a typo in a literal silently grants nothing.

Need a new permission code for your module? Ask, and it gets added to
`Permissions.java` plus `02_seed_data.sql` in one commit.

## 3. Database connection

```java
import com.pos.core.db.ConnectionFactory;
import com.pos.core.exception.DataAccessException;

try (Connection conn = ConnectionFactory.getConnection();
     PreparedStatement ps = conn.prepareStatement(SQL)) {
    ...
} catch (SQLException e) {
    throw new DataAccessException("Could not load products.", e);
}
```

Do not call `DriverManager` yourself and do not write your own connection
class. Configuration comes from `db.properties` or the `POS_DB_*` environment
variables.

Never let a `SQLException` escape a DAO — wrap it in `DataAccessException`
with a message a cashier could read.

## 4. Foreign keys to users

`users.id` is `INT` and will stay `INT`.

```sql
user_id INT NOT NULL,
CONSTRAINT fk_sales_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT
```

Users who own historical records are never deleted, only deactivated
(`active = FALSE`). That keeps every sale attributable.

## 5. Adding your screen to the navigation

1. Put your FXML anywhere under `resources/fxml/<your-module>/`.
2. In `ui/layout/SidebarController.java`, replace the placeholder line in your
   own handler:

```java
@FXML
private void onProducts() {
    select(productsNavButton);
    mainLayout.loadView("/fxml/products/products-view.fxml");   // was showModulePlaceholder(...)
}
```

That one line is the only change you need in a shared file. Everything else —
top bar, sidebar, status bar, content area, styling — already works.

Your view must be a `StackPane` or `VBox` root with no frame of its own: no
top bar, no sidebar, no status bar, no `Stage`.

Sidebar visibility is already wired to permissions, so your item appears only
for roles that hold the matching code.

## 6. Shared UI you should reuse, not rebuild

| Class | Use for |
|---|---|
| `ui.components.AppButton` | every button (`variant="PRIMARY\|SECONDARY\|DANGER\|SUCCESS"`) |
| `ui.components.AppTextField` / `AppPasswordField` | every text input |
| `ui.components.AppTableView` | every data table |
| `ui.components.AppCard` | white surface panels |
| `ui.components.ConfirmDialog` | yes/no confirmations |
| `ui.components.Toast` | short success/error feedback |
| `ui.components.LoadingOverlay` | loading state |
| `ui.components.PlaceholderNode` | empty and error states |
| `ui.theme.ThemeManager` | applying the stylesheets to a new Scene |
| `com.pos.core.util.AlertUtil.userMessage(e)` | turning an exception into safe user text |

Do not add hex colours, fonts or inline `-fx-style` anywhere. If the shared
system is missing something you need, raise it — do not fork it.

## 7. Long database calls

Any DAO call from a controller belongs on a background `Task`, with
`LoadingOverlay` shown while it runs. `UserListController` is a working
example you can copy.
