package com.pos.auth.dao;

import com.pos.core.db.ConnectionFactory;
import com.pos.core.exception.DataAccessException;
import com.pos.auth.model.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionDaoImpl implements PermissionDao {

    private static final String SELECT_ALL =
            "SELECT id, code, description FROM permissions ORDER BY code";

    private static final String SELECT_CODES_BY_ROLE =
            "SELECT p.code "
          + "FROM permissions p "
          + "JOIN role_permissions rp ON rp.permission_id = p.id "
          + "WHERE rp.role_id = ?";

    @Override
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Permission(rs.getInt("id"), rs.getString("code"),
                        rs.getString("description")));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل الصلاحيات.", e);
        }
    }

    @Override
    public Set<String> findCodesByRoleId(int roleId) {
        Set<String> codes = new HashSet<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_CODES_BY_ROLE)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    codes.add(rs.getString("code"));
                }
            }
            return codes;
        } catch (SQLException e) {
            throw new DataAccessException("تعذّر تحميل صلاحيات الدور.", e);
        }
    }
}
