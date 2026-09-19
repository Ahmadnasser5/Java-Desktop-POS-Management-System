package com.pos.auth.service;

import com.pos.auth.exception.AuthorizationException;
import com.pos.auth.model.Role;
import com.pos.auth.security.SessionManager;

import java.util.Locale;

/**
 * The one place that answers "is the current user allowed to do this?".
 * <p>
 * SHARED ENTRY POINT for the other four modules. Never copy role checks into
 * a controller - call these methods instead.
 *
 * <pre>{@code
 * if (AuthorizationService.hasPermission(Permissions.SALE_CANCEL)) { ... }
 * AuthorizationService.requirePermission(Permissions.USER_MANAGE); // throws if denied
 * }</pre>
 */
public final class AuthorizationService {

    private AuthorizationService() {
    }

    public static boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static boolean isSales() {
        return hasRole(Role.SALES);
    }

    public static boolean hasRole(String roleName) {
        String current = SessionManager.getInstance().getCurrentRoleName();
        return current != null && current.equalsIgnoreCase(roleName);
    }

    /**
     * @return true when the current user's role grants the permission code.
     *         Returns false when nobody is logged in.
     */
    public static boolean hasPermission(String permissionCode) {
        if (permissionCode == null || !isLoggedIn()) {
            return false;
        }
        return SessionManager.getInstance().getPermissions()
                .contains(permissionCode.toUpperCase(Locale.ROOT));
    }

    public static boolean hasAnyPermission(String... permissionCodes) {
        if (permissionCodes == null) {
            return false;
        }
        for (String code : permissionCodes) {
            if (hasPermission(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enforcement version - call this at the top of every protected service
     * method. Hiding a button is UX; this is the actual security boundary.
     *
     * @throws AuthorizationException when the permission is missing
     */
    public static void requirePermission(String permissionCode) {
        if (!hasPermission(permissionCode)) {
            throw new AuthorizationException(
                    "You do not have permission to perform this action.");
        }
    }
}
