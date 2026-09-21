package com.pos.auth.service;

import com.pos.auth.dao.PermissionDao;
import com.pos.auth.dao.UserDao;
import com.pos.auth.exception.AuthenticationException;
import com.pos.auth.exception.ValidationException;
import com.pos.auth.model.Role;
import com.pos.auth.model.User;
import com.pos.auth.security.BCryptPasswordHasher;
import com.pos.auth.security.PasswordHasher;
import com.pos.auth.security.Permissions;
import com.pos.auth.security.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Role ADMIN_ROLE = new Role(1, Role.ADMIN, "Full access");
    private static final String PASSWORD = "Admin@123";

    @Mock private UserDao userDao;
    @Mock private PermissionDao permissionDao;

    private final PasswordHasher hasher = new BCryptPasswordHasher();
    private AuthService authService;
    private String storedHash;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDao, permissionDao, hasher);
        storedHash = hasher.hash(PASSWORD.toCharArray());
        SessionManager.getInstance().logout();
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
    }

    private User activeAdmin() {
        User user = new User();
        user.setId(7);
        user.setUsername("admin");
        user.setPasswordHash(storedHash);
        user.setRole(ADMIN_ROLE);
        user.setActive(true);
        return user;
    }

    @Test
    void correctCredentialsOpenASession() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));
        when(permissionDao.findCodesByRoleId(1)).thenReturn(Set.of(Permissions.USER_MANAGE));

        User result = authService.login("admin", PASSWORD.toCharArray());

        assertEquals(7, result.getId());
        assertEquals("admin", result.getUsername());
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals(7, SessionManager.getInstance().getCurrentUserId());
        assertEquals(Role.ADMIN, SessionManager.getInstance().getCurrentRoleName());
        assertTrue(AuthorizationService.isAdmin());
        assertTrue(AuthorizationService.hasPermission(Permissions.USER_MANAGE));
        verify(userDao).updateLastLogin(7);
    }

    @Test
    void theSessionNeverCarriesThePasswordHash() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));
        when(permissionDao.findCodesByRoleId(1)).thenReturn(Set.of());

        authService.login("admin", PASSWORD.toCharArray());

        assertNull(SessionManager.getInstance().getCurrentUser().getPasswordHash());
    }

    @Test
    void wrongPasswordIsRejected() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));

        assertThrows(AuthenticationException.class,
                () -> authService.login("admin", "WrongPass1".toCharArray()));

        assertFalse(SessionManager.getInstance().isLoggedIn());
        verify(userDao, never()).updateLastLogin(anyInt());
    }

    @Test
    void unknownUsernameGivesTheSameMessageAsAWrongPassword() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));

        String unknownMessage = assertThrows(AuthenticationException.class,
                () -> authService.login("ghost", PASSWORD.toCharArray())).getMessage();

        String wrongPasswordMessage = assertThrows(AuthenticationException.class,
                () -> authService.login("admin", "WrongPass1".toCharArray())).getMessage();

        assertEquals(unknownMessage, wrongPasswordMessage,
                "the two failures must be indistinguishable to an attacker");
    }

    @Test
    void inactiveUserCannotSignIn() {
        User inactive = activeAdmin();
        inactive.setActive(false);
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(inactive));

        AuthenticationException error = assertThrows(AuthenticationException.class,
                () -> authService.login("admin", PASSWORD.toCharArray()));

        assertTrue(error.getMessage().toLowerCase().contains("deactivated"));
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void emptyFieldsAreRejectedBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class,
                () -> authService.login("  ", PASSWORD.toCharArray()));
        assertThrows(ValidationException.class,
                () -> authService.login("admin", new char[0]));
        verify(userDao, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void logoutClearsTheSession() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));
        when(permissionDao.findCodesByRoleId(1)).thenReturn(Set.of(Permissions.USER_MANAGE));

        authService.login("admin", PASSWORD.toCharArray());
        authService.logout();

        assertFalse(SessionManager.getInstance().isLoggedIn());
        assertNull(SessionManager.getInstance().getCurrentUser());
        assertEquals(-1, SessionManager.getInstance().getCurrentUserId());
        assertFalse(AuthorizationService.hasPermission(Permissions.USER_MANAGE));
    }

    @Test
    void changingMyOwnPasswordRequiresTheCurrentOne() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(activeAdmin()));
        when(permissionDao.findCodesByRoleId(1)).thenReturn(Set.of());
        lenient().when(userDao.findById(7)).thenReturn(Optional.of(activeAdmin()));

        authService.login("admin", PASSWORD.toCharArray());

        assertThrows(AuthenticationException.class,
                () -> authService.changeOwnPassword("nope".toCharArray(), "NewPass123".toCharArray()));

        authService.changeOwnPassword(PASSWORD.toCharArray(), "NewPass123".toCharArray());
        verify(userDao).updatePasswordHash(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(false));
    }
}
