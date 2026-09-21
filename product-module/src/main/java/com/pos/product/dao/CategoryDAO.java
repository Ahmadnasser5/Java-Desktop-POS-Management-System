package com.pos.product.dao;

import com.pos.product.model.Category;
import com.pos.product.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private String lastError;

    public String getLastError() {
        return lastError;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        lastError = null;
        String sql = "SELECT id, name FROM categories ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
        return list;
    }

    public boolean addCategory(String name) {
        lastError = null;
        String sql = "INSERT INTO categories (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(int id) {
        lastError = null;
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }
}
