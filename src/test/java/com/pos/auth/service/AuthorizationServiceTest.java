package com.pos.auth.service;

import com.pos.auth.model.Role;
import com.pos.auth.model.User;
import com.pos.auth.exception.AuthorizationException;
import com.pos.auth.security.Permissions;
import com.pos.auth.security.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
    }

    private void loginAs(String roleName, Set<String> permissions) {
        Role role = new Role(roleName.equals(Role.ADMIN) ? 1 : 2, roleName, null);
        User user = new User(5, "tester", "Tester", role, true);
        SessionManager.getInstance().login(user, permissions);
    }

    @Test
    void nobodyLoggedInMeansNoPermissions() {
        assertFalse(AuthorizationService.isLoggedIn());
        assertFalse(AuthorizationService.isAdmin());
        assertFalse(AuthorizationService.hasPermission(Permissions.SALE_CREATE));
        assertThrows(AuthorizationException.class,
                () -> AuthorizationService.requirePermission(Permissions.SALE_CREATE));
    }

    @Test
    void adminRoleIsDetected() {
        loginAs(Role.ADMIN, Set.of(Permissions.USER_MANAGE));
        assertTrue(AuthorizationService.isAdmin());
        assertFalse(AuthorizationService.isSales());
    }

    @Test
    void salesCannotManageUsers() {
        loginAs(Role.SALES, Set.of(Permissions.SALE_CREATE, Permissions.PRODUCT_VIEW));

        assertTrue(AuthorizationService.isSales());
        assertTrue(AuthorizationService.hasPermission(Permissions.SALE_CREATE));
        assertFalse(AuthorizationService.hasPermission(Permissions.USER_MANAGE));
        assertThrows(AuthorizationException.class,
                () -> AuthorizationService.requirePermission(Permissions.USER_MANAGE));
    }

    @Test
    void permissionCheckIsCaseInsensitive() {
        loginAs(Role.SALES, Set.of("sale_create"));
        assertTrue(AuthorizationService.hasPermission(Permissions.SALE_CREATE));
        assertDoesNotThrow(() -> AuthorizationService.requirePermission("Sale_Create"));
    }

    @Test
    void hasAnyPermissionWorks() {
        loginAs(Role.SALES, Set.of(Permissions.SALE_CREATE));
        assertTrue(AuthorizationService.hasAnyPermission(
                Permissions.USER_MANAGE, Permissions.SALE_CREATE));
        assertFalse(AuthorizationService.hasAnyPermission(
                Permissions.USER_MANAGE, Permissions.REPORT_VIEW));
    }
}
