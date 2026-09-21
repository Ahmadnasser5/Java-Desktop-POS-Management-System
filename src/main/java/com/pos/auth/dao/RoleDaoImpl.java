package com.pos.auth.dao;

import com.pos.core.db.ConnectionFactory;
import com.pos.core.exception.DataAccessException;
import com.pos.auth.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDaoImpl implements RoleDao {

    private static final String SELECT_ALL =
            "SELECT id, name, description FROM roles ORDER BY name";

    private static final String SELECT_BY_ID =
            "SELECT id, name, description FROM roles WHERE id = ?";

    private static final String SELECT_BY_NAME =
            "SELECT id, name, description FROM roles WHERE name = ?";

    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(map(rs));
            }
            return roles;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل الأدوار.", e);
        }
    }

    @Override
    public Optional<Role> findById(int id) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل بيانات الدور.", e);
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل بيانات الدور.", e);
        }
    }

    static Role map(ResultSet rs) throws SQLException {
        return new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
    }
}
