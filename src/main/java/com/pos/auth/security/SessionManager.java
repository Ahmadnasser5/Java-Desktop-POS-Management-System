package com.pos.auth.security;

import com.pos.auth.model.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Holds the currently logged-in user for the lifetime of the application
 * process. In-memory only - nothing is written to disk.
 * <p>
 * SHARED ENTRY POINT for the other four modules:
 * <pre>{@code
 * int cashierId = SessionManager.getInstance().getCurrentUserId();
 * }</pre>
 *
 * <p>The stored {@link User} never carries a password hash (see
 * {@link User#withoutPasswordHash()}) and never carries a plaintext password.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private Set<String> permissions = Collections.emptySet();
    private long loginTimeMillis;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a session. Called only by {@code AuthService} after the password
     * has been verified.
     *
     * @param user            the authenticated user
     * @param userPermissions permission codes granted through the user's role
     */
    public synchronized void login(User user, Set<String> userPermissions) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        this.currentUser = user.withoutPasswordHash();
        Set<String> upper = new HashSet<>();
        if (userPermissions != null) {
            for (String code : userPermissions) {
                if (code != null) {
                    upper.add(code.toUpperCase(Locale.ROOT));
                }
            }
        }
        this.permissions = Collections.unmodifiableSet(upper);
        this.loginTimeMillis = System.currentTimeMillis();
    }

    public synchronized void logout() {
        this.currentUser = null;
        this.permissions = Collections.emptySet();
        this.loginTimeMillis = 0L;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    /** @return the logged-in user, or {@code null} when nobody is logged in */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /** @return the user id, or -1 when nobody is logged in */
    public synchronized int getCurrentUserId() {
        return currentUser == null ? -1 : currentUser.getId();
    }

    public synchronized String getCurrentUsername() {
        return currentUser == null ? null : currentUser.getUsername();
    }

    /** @return "ADMIN" / "SALES", or {@code null} when nobody is logged in */
    public synchronized String getCurrentRoleName() {
        return currentUser == null ? null : currentUser.getRoleName();
    }

    public synchronized Set<String> getPermissions() {
        return permissions;
    }

    public synchronized long getLoginTimeMillis() {
        return loginTimeMillis;
    }

    /** Keeps the session in sync after an admin edits their own account. */
    public synchronized void refreshCurrentUser(User updated) {
        if (currentUser != null && updated != null && updated.getId() == currentUser.getId()) {
            this.currentUser = updated.withoutPasswordHash();
        }
    }
}
