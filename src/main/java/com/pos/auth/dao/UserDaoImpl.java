package com.pos.auth.dao;

import com.pos.core.db.ConnectionFactory;
import com.pos.core.exception.DataAccessException;
import com.pos.auth.model.Role;
import com.pos.auth.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation. Every statement is a {@link PreparedStatement};
 * no SQL string is ever built by concatenating user input.
 */
public class UserDaoImpl implements UserDao {

    private static final String BASE_SELECT =
            "SELECT u.id, u.username, u.password_hash, u.full_name, u.active, "
          + "       u.must_change_password, u.last_login_at, u.created_at, u.updated_at, "
          + "       r.id AS role_id, r.name AS role_name, r.description AS role_description "
          + "FROM users u "
          + "JOIN roles r ON r.id = u.role_id ";

    private static final String SELECT_BY_USERNAME = BASE_SELECT + "WHERE u.username = ?";
    private static final String SELECT_BY_ID       = BASE_SELECT + "WHERE u.id = ?";
    private static final String SELECT_ALL         = BASE_SELECT + "ORDER BY u.username";
    private static final String SEARCH             = BASE_SELECT
            + "WHERE LOWER(u.username) LIKE ? OR LOWER(COALESCE(u.full_name, '')) LIKE ? "
            + "ORDER BY u.username";

    private static final String INSERT =
            "INSERT INTO users (username, password_hash, full_name, role_id, active, must_change_password) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE users SET username = ?, full_name = ?, role_id = ?, active = ? WHERE id = ?";

    private static final String UPDATE_ACTIVE =
            "UPDATE users SET active = ? WHERE id = ?";

    private static final String UPDATE_PASSWORD =
            "UPDATE users SET password_hash = ?, must_change_password = ? WHERE id = ?";

    private static final String UPDATE_LAST_LOGIN =
            "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String EXISTS_USERNAME =
            "SELECT 1 FROM users WHERE username = ? LIMIT 1";

    private static final String EXISTS_USERNAME_EXCL =
            "SELECT 1 FROM users WHERE username = ? AND id <> ? LIMIT 1";

    private static final String DELETE = "DELETE FROM users WHERE id = ?";

    private static final String COUNT_ACTIVE_ADMINS =
            "SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.role_id "
          + "WHERE r.name = 'ADMIN' AND u.active = TRUE";

    // ------------------------------------------------------------------ read

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر قراءة بيانات المستخدم.", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر قراءة بيانات المستخدم.", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل قائمة المستخدمين.", e);
        }
    }

    @Override
    public List<User> search(String term) {
        String pattern = "%" + (term == null ? "" : term.toLowerCase()) + "%";
        List<User> users = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر البحث عن المستخدمين.", e);
        }
    }

    // ----------------------------------------------------------------- write

    @Override
    public int insert(User user, String passwordHash) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, passwordHash);
            ps.setString(3, user.getFullName());
            ps.setInt(4, user.getRole().getId());
            ps.setBoolean(5, user.isActive());
            ps.setBoolean(6, user.isMustChangePassword());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("تم إنشاء المستخدم لكن لم يتم إرجاع معرّف له.");
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر إنشاء المستخدم.", e);
        }
    }

    @Override
    public void update(User user) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setInt(3, user.getRole().getId());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر حفظ بيانات المستخدم.", e);
        }
    }

    @Override
    public void updateActive(int userId, boolean active) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ACTIVE)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تغيير حالة الحساب.", e);
        }
    }

    @Override
    public void updatePasswordHash(int userId, String passwordHash, boolean mustChangePassword) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, passwordHash);
            ps.setBoolean(2, mustChangePassword);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تغيير كلمة المرور.", e);
        }
    }

    @Override
    public void updateLastLogin(int userId) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Never block a successful login because of a bookkeeping column.
            throw new DataAccessException("تعذّر تسجيل وقت الدخول.", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر التحقق من اسم المستخدم.", e);
        }
    }

    @Override
    public boolean existsByUsernameExcludingId(String username, int userId) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_USERNAME_EXCL)) {
            ps.setString(1, username);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر التحقق من اسم المستخدم.", e);
        }
    }

    @Override
    public void deleteById(int userId) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Typically a foreign key from sales/payments - tell the user to deactivate.
            throw new DataAccessException(
                    "لا يمكن حذف هذا المستخدم لأن لديه سجلات مرتبطة به. "
                            + "قم بإيقاف الحساب بدلاً من ذلك.", e);
        }
    }

    @Override
    public int countActiveAdmins() {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ACTIVE_ADMINS);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر إحصاء عدد المديرين.", e);
        }
    }

    // --------------------------------------------------------------- mapping

    private User map(ResultSet rs) throws SQLException {
        Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"),
                rs.getString("role_description"));

        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(role);
        user.setActive(rs.getBoolean("active"));
        user.setMustChangePassword(rs.getBoolean("must_change_password"));
        user.setLastLoginAt(toLocalDateTime(rs.getTimestamp("last_login_at")));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return user;
    }

    private static java.time.LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
