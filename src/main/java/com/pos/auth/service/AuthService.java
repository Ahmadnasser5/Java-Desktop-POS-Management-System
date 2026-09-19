package com.pos.auth.service;

import com.pos.auth.dao.PermissionDao;
import com.pos.auth.dao.PermissionDaoImpl;
import com.pos.auth.dao.UserDao;
import com.pos.auth.dao.UserDaoImpl;
import com.pos.auth.exception.AuthenticationException;
import com.pos.auth.exception.ValidationException;
import com.pos.auth.model.User;
import com.pos.auth.security.BCryptPasswordHasher;
import com.pos.auth.security.PasswordHasher;
import com.pos.auth.security.SessionManager;
import com.pos.core.exception.DataAccessException;
import com.pos.core.util.ValidationUtil;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authentication use cases: log in, log out, change my own password.
 * <p>
 * Contains no JavaFX code, which is what makes it unit-testable.
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    /**
     * A throw-away BCrypt hash used when the username does not exist, so an
     * unknown username costs the same time as a wrong password. Without this,
     * an attacker can enumerate valid usernames by measuring response time.
     */
    private static final String DUMMY_HASH =
            "$2a$12$7l.oN7KkX7mfhTl4ptB6ee0Kh1m6IjRNhyivCZOm7/XZlnhlkkRcK";

    /** Same text for "unknown user" and "wrong password" - never leak which. */
    private static final String GENERIC_FAILURE = "اسم المستخدم أو كلمة المرور غير صحيحة.";

    private final UserDao userDao;
    private final PermissionDao permissionDao;
    private final PasswordHasher passwordHasher;

    public AuthService() {
        this(new UserDaoImpl(), new PermissionDaoImpl(), new BCryptPasswordHasher());
    }

    public AuthService(UserDao userDao, PermissionDao permissionDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.permissionDao = permissionDao;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Full login flow:
     * validate input -> load user -> check active -> verify hash ->
     * load permissions -> open session.
     *
     * @return the authenticated user (without password hash)
     * @throws ValidationException     when a field is empty
     * @throws AuthenticationException when the credentials are rejected
     * @throws DataAccessException     when the database is unreachable
     */
    public User login(String username, char[] password) {
        if (ValidationUtil.isBlank(username)) {
            throw new ValidationException("من فضلك أدخل اسم المستخدم.");
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("من فضلك أدخل كلمة المرور.");
        }

        String trimmed = username.trim();
        Optional<User> found = userDao.findByUsername(trimmed);

        if (found.isEmpty()) {
            // Spend the same time as a real verification, then fail.
            passwordHasher.verify(password, DUMMY_HASH);
            LOG.log(Level.INFO, "Failed login attempt for unknown username.");
            throw new AuthenticationException(GENERIC_FAILURE);
        }

        User user = found.get();

        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            LOG.log(Level.INFO, "Failed login attempt for an existing account.");
            throw new AuthenticationException(GENERIC_FAILURE);
        }

        if (!user.isActive()) {
            // Different message on purpose: the credentials were correct, so
            // nothing is leaked, and the real employee needs to know why.
            throw new AuthenticationException(
                    "هذا الحساب موقوف. من فضلك تواصل مع المسؤول.");
        }

        Set<String> permissions = permissionDao.findCodesByRoleId(user.getRole().getId());
        SessionManager.getInstance().login(user, permissions);

        try {
            userDao.updateLastLogin(user.getId());
        } catch (DataAccessException e) {
            // Bookkeeping only - a failure here must not block the cashier.
            LOG.log(Level.WARNING, "Could not update last_login_at", e);
        }

        return SessionManager.getInstance().getCurrentUser();
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    /**
     * Lets the logged-in user change their own password. Requires the current
     * password, so a walk-up attacker on an unlocked terminal cannot lock the
     * real owner out.
     */
    public void changeOwnPassword(char[] currentPassword, char[] newPassword) {
        User sessionUser = SessionManager.getInstance().getCurrentUser();
        if (sessionUser == null) {
            throw new AuthenticationException("لم يتم تسجيل الدخول.");
        }

        User stored = userDao.findById(sessionUser.getId())
                .orElseThrow(() -> new AuthenticationException("هذا الحساب لم يعد موجوداً."));

        if (!passwordHasher.verify(currentPassword, stored.getPasswordHash())) {
            throw new AuthenticationException("كلمة المرور الحالية غير صحيحة.");
        }
        if (!ValidationUtil.isStrongEnough(newPassword)) {
            throw new ValidationException(ValidationUtil.passwordPolicyMessage());
        }

        userDao.updatePasswordHash(stored.getId(), passwordHasher.hash(newPassword), false);

        stored.setMustChangePassword(false);
        SessionManager.getInstance().refreshCurrentUser(stored);
    }
}
