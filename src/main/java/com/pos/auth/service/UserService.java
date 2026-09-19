package com.pos.auth.service;

import com.pos.auth.dao.RoleDao;
import com.pos.auth.dao.RoleDaoImpl;
import com.pos.auth.dao.UserDao;
import com.pos.auth.dao.UserDaoImpl;
import com.pos.auth.exception.ValidationException;
import com.pos.auth.model.Role;
import com.pos.auth.model.User;
import com.pos.auth.security.BCryptPasswordHasher;
import com.pos.auth.security.PasswordHasher;
import com.pos.auth.security.Permissions;
import com.pos.auth.security.SessionManager;

import java.util.List;
import java.util.Optional;

/**
 * User management use cases (admin side).
 * <p>
 * Every public method starts with an authorization check, so the rules hold
 * even if a UI element is accidentally left visible.
 */
public class UserService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordHasher passwordHasher;

    public UserService() {
        this(new UserDaoImpl(), new RoleDaoImpl(), new BCryptPasswordHasher());
    }

    public UserService(UserDao userDao, RoleDao roleDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordHasher = passwordHasher;
    }

    // ------------------------------------------------------------------ read

    public List<User> findAll() {
        AuthorizationService.requirePermission(Permissions.USER_VIEW);
        return userDao.findAll();
    }

    public List<User> search(String term) {
        AuthorizationService.requirePermission(Permissions.USER_VIEW);
        if (term == null || term.trim().isEmpty()) {
            return userDao.findAll();
        }
        return userDao.search(term.trim());
    }

    public List<Role> findAllRoles() {
        AuthorizationService.requirePermission(Permissions.USER_VIEW);
        return roleDao.findAll();
    }

    public Optional<User> findById(int id) {
        AuthorizationService.requirePermission(Permissions.USER_VIEW);
        return userDao.findById(id);
    }

    // ----------------------------------------------------------------- write

    /**
     * Creates a user. The new account is always flagged
     * {@code must_change_password}, so the administrator never knows the
     * password the employee ends up using.
     *
     * @return the id of the new user
     */
    public int createUser(String username, char[] password, String fullName,
                          int roleId, boolean active) {
        AuthorizationService.requirePermission(Permissions.USER_MANAGE);

        String cleanUsername = username == null ? "" : username.trim();
        validateUsername(cleanUsername);
        validatePassword(password);

        if (userDao.existsByUsername(cleanUsername)) {
            throw new ValidationException("اسم المستخدم هذا مستخدم بالفعل.");
        }

        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new ValidationException("من فضلك اختر دوراً صحيحاً."));

        User user = new User();
        user.setUsername(cleanUsername);
        user.setFullName(trimOrNull(fullName));
        user.setRole(role);
        user.setActive(active);
        user.setMustChangePassword(true);

        return userDao.insert(user, passwordHasher.hash(password));
    }

    /** Updates username, full name, role and active flag. */
    public void updateUser(int userId, String username, String fullName,
                           int roleId, boolean active) {
        AuthorizationService.requirePermission(Permissions.USER_MANAGE);

        String cleanUsername = username == null ? "" : username.trim();
        validateUsername(cleanUsername);

        User existing = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("هذا المستخدم لم يعد موجوداً."));

        if (userDao.existsByUsernameExcludingId(cleanUsername, userId)) {
            throw new ValidationException("اسم المستخدم هذا مستخدم بالفعل.");
        }

        Role newRole = roleDao.findById(roleId)
                .orElseThrow(() -> new ValidationException("من فضلك اختر دوراً صحيحاً."));

        boolean wasActiveAdmin = existing.getRole().isAdmin() && existing.isActive();
        boolean staysActiveAdmin = newRole.isAdmin() && active;
        if (wasActiveAdmin && !staysActiveAdmin && userDao.countActiveAdmins() <= 1) {
            throw new ValidationException(
                    "هذا آخر مدير نظام مفعّل. من فضلك أنشئ مديراً آخر أولاً.");
        }

        existing.setUsername(cleanUsername);
        existing.setFullName(trimOrNull(fullName));
        existing.setRole(newRole);
        existing.setActive(active);

        userDao.update(existing);
        SessionManager.getInstance().refreshCurrentUser(existing);
    }

    public void setActive(int userId, boolean active) {
        AuthorizationService.requirePermission(Permissions.USER_MANAGE);

        User existing = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("هذا المستخدم لم يعد موجوداً."));

        if (!active && userId == SessionManager.getInstance().getCurrentUserId()) {
            throw new ValidationException("لا يمكنك إيقاف حسابك الخاص.");
        }
        if (!active && existing.getRole().isAdmin() && userDao.countActiveAdmins() <= 1) {
            throw new ValidationException(
                    "هذا آخر مدير نظام مفعّل. لا يمكن إيقاف هذا الحساب.");
        }

        userDao.updateActive(userId, active);
    }

    /** Administrative password reset. The user must change it at next login. */
    public void resetPassword(int userId, char[] newPassword) {
        AuthorizationService.requirePermission(Permissions.USER_MANAGE);
        validatePassword(newPassword);

        userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("هذا المستخدم لم يعد موجوداً."));

        userDao.updatePasswordHash(userId, passwordHasher.hash(newPassword), true);
    }

    /**
     * Hard delete. Prefer {@link #setActive(int, boolean)} - a deleted user
     * breaks the foreign keys owned by the sales and payments modules, so the
     * DAO will refuse when historical records exist.
     */
    public void deleteUser(int userId) {
        AuthorizationService.requirePermission(Permissions.USER_MANAGE);

        if (userId == SessionManager.getInstance().getCurrentUserId()) {
            throw new ValidationException("لا يمكنك حذف حسابك الخاص.");
        }

        User existing = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("هذا المستخدم لم يعد موجوداً."));

        if (existing.getRole().isAdmin() && existing.isActive()
                && userDao.countActiveAdmins() <= 1) {
            throw new ValidationException(
                    "هذا آخر مدير نظام مفعّل. لا يمكن حذف هذا الحساب.");
        }

        userDao.deleteById(userId);
    }

    // ------------------------------------------------------------ validation

    private void validateUsername(String username) {
        if (username.isEmpty()) {
            throw new ValidationException("اسم المستخدم مطلوب.");
        }
        if (!com.pos.core.util.ValidationUtil.isValidUsername(username)) {
            throw new ValidationException(
                    "يجب أن يكون اسم المستخدم من 3 إلى 50 حرفاً، ويمكن أن يحتوي فقط على "
                            + "حروف إنجليزية وأرقام ونقطة وشرطة وشرطة سفلية.");
        }
    }

    private void validatePassword(char[] password) {
        if (!com.pos.core.util.ValidationUtil.isStrongEnough(password)) {
            throw new ValidationException(com.pos.core.util.ValidationUtil.passwordPolicyMessage());
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
