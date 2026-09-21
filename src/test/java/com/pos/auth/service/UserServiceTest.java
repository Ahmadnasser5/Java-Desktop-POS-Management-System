package com.pos.auth.service;

import com.pos.auth.dao.RoleDao;
import com.pos.auth.dao.UserDao;
import com.pos.auth.exception.AuthorizationException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    private static final Role ADMIN_ROLE = new Role(1, Role.ADMIN, null);
    private static final Role SALES_ROLE = new Role(2, Role.SALES, null);

    @Mock private UserDao userDao;
    @Mock private RoleDao roleDao;

    private final PasswordHasher hasher = new BCryptPasswordHasher();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao, roleDao, hasher);
        when(roleDao.findById(1)).thenReturn(Optional.of(ADMIN_ROLE));
        when(roleDao.findById(2)).thenReturn(Optional.of(SALES_ROLE));
        loginAsAdmin();
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
    }

    private void loginAsAdmin() {
        SessionManager.getInstance().login(
                new User(1, "admin", "Admin", ADMIN_ROLE, true),
                Set.of(Permissions.USER_VIEW, Permissions.USER_MANAGE));
    }

    private void loginAsSales() {
        SessionManager.getInstance().login(
                new User(9, "cashier", "Cashier", SALES_ROLE, true),
                Set.of(Permissions.SALE_CREATE));
    }

    @Test
    void newUsersAreStoredHashedAndFlaggedForPasswordChange() {
        when(userDao.existsByUsername("sara")).thenReturn(false);
        when(userDao.insert(any(User.class), anyString())).thenReturn(42);

        int id = userService.createUser("sara", "Cashier1".toCharArray(), "Sara", 2, true);

        assertEquals(42, id);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userDao).insert(userCaptor.capture(), hashCaptor.capture());

        assertEquals("sara", userCaptor.getValue().getUsername());
        assertTrue(userCaptor.getValue().isMustChangePassword());
        assertTrue(hashCaptor.getValue().startsWith("$2"));
        assertTrue(hasher.verify("Cashier1".toCharArray(), hashCaptor.getValue()));
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(userDao.existsByUsername("sara")).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> userService.createUser("sara", "Cashier1".toCharArray(), null, 2, true));
        verify(userDao, never()).insert(any(), anyString());
    }

    @Test
    void weakPasswordIsRejected() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("sara", "123".toCharArray(), null, 2, true));
    }

    @Test
    void invalidUsernameIsRejected() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("a b!", "Cashier1".toCharArray(), null, 2, true));
    }

    @Test
    void salesUserCannotManageUsers() {
        loginAsSales();
        assertThrows(AuthorizationException.class, () -> userService.findAll());
        assertThrows(AuthorizationException.class,
                () -> userService.createUser("sara", "Cashier1".toCharArray(), null, 2, true));
    }

    @Test
    void youCannotDeactivateYourOwnAccount() {
        when(userDao.findById(1)).thenReturn(
                Optional.of(new User(1, "admin", "Admin", ADMIN_ROLE, true)));

        assertThrows(ValidationException.class, () -> userService.setActive(1, false));
        verify(userDao, never()).updateActive(1, false);
    }

    @Test
    void theLastActiveAdminCannotBeDeactivated() {
        User otherAdmin = new User(3, "admin2", "Other", ADMIN_ROLE, true);
        when(userDao.findById(3)).thenReturn(Optional.of(otherAdmin));
        when(userDao.countActiveAdmins()).thenReturn(1);

        assertThrows(ValidationException.class, () -> userService.setActive(3, false));
    }

    @Test
    void theLastActiveAdminCannotBeDemoted() {
        User otherAdmin = new User(3, "admin2", "Other", ADMIN_ROLE, true);
        when(userDao.findById(3)).thenReturn(Optional.of(otherAdmin));
        when(userDao.existsByUsernameExcludingId("admin2", 3)).thenReturn(false);
        when(userDao.countActiveAdmins()).thenReturn(1);

        assertThrows(ValidationException.class,
                () -> userService.updateUser(3, "admin2", "Other", 2, true));
    }

    @Test
    void adminPasswordResetForcesAChangeAtNextLogin() {
        when(userDao.findById(3)).thenReturn(
                Optional.of(new User(3, "sara", "Sara", SALES_ROLE, true)));

        userService.resetPassword(3, "Temp1234".toCharArray());

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userDao).updatePasswordHash(org.mockito.ArgumentMatchers.eq(3),
                hashCaptor.capture(), org.mockito.ArgumentMatchers.eq(true));
        assertTrue(hasher.verify("Temp1234".toCharArray(), hashCaptor.getValue()));
    }
}
