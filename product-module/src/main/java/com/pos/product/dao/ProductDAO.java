package com.pos.product.dao;

import com.pos.product.model.Product;
import com.pos.product.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String BASE_SELECT =
            "SELECT p.id, p.name, p.barcode, p.price, p.stock_quantity, p.min_stock_level, " +
            "       p.category_id, c.name AS category_name " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id ";

    /** Holds a human-readable reason after the last failed call, for the UI's Error state. */
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        lastError = null;
        String sql = BASE_SELECT + "ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
        }
        return list;
    }

    /** Search by product name or barcode (used by the search box and by barcode-scanner input). */
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        lastError = null;
        String sql = BASE_SELECT + "WHERE p.name LIKE ? OR p.barcode LIKE ? ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
        }
        return list;
    }

    /** Exact-match lookup by barcode - used when a scanner reads a code at the POS screen. */
    public Product findByBarcode(String barcode) {
        lastError = null;
        String sql = BASE_SELECT + "WHERE p.barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
        }
        return null;
    }

    /** Products whose stock_quantity has dropped to or below their min_stock_level. */
    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        lastError = null;
        String sql = BASE_SELECT + "WHERE p.stock_quantity <= p.min_stock_level ORDER BY p.stock_quantity ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
        }
        return list;
    }

    /** All products belonging to a given category - used by the category management screen. */
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.category_id = ? ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addProduct(Product p) {
        lastError = null;
        String sql = "INSERT INTO products (name, barcode, price, stock_quantity, min_stock_level, category_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindProductFields(ps, p);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product p) {
        lastError = null;
        String sql = "UPDATE products SET name = ?, barcode = ?, price = ?, stock_quantity = ?, " +
                     "min_stock_level = ?, category_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindProductFields(ps, p);
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        lastError = null;
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Called by the POS & Sales module (Engineer 3) after a sale is completed, to deduct
     * the sold quantity from stock. The WHERE clause guards against overselling: the update
     * only applies (and returns true) if there is enough stock, so it also works safely if
     * two sales race against each other.
     */
    public boolean reduceStock(int productId, int soldQty) {
        lastError = null;
        if (soldQty <= 0) {
            lastError = "Quantity sold must be greater than zero.";
            return false;
        }

        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? " +
                     "WHERE id = ? AND stock_quantity >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, soldQty);
            ps.setInt(2, productId);
            ps.setInt(3, soldQty);
            boolean updated = ps.executeUpdate() > 0;
            if (!updated) {
                lastError = "Not enough stock to complete this sale.";
            }
            return updated;
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
            return false;
        }
    }

    /** Restocks a product (e.g. a new delivery arrives). Positive delta only. */
    public boolean addStock(int productId, int deliveredQty) {
        lastError = null;
        if (deliveredQty <= 0) {
            lastError = "Delivered quantity must be greater than zero.";
            return false;
        }

        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveredQty);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = describe(e);
            e.printStackTrace();
            return false;
        }
    }

    // --- helpers ---

    private void bindProductFields(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setString(2, p.getBarcode());
        ps.setDouble(3, p.getPrice());
        ps.setInt(4, p.getStockQuantity());
        ps.setInt(5, p.getMinStockLevel());
        if (p.getCategoryId() > 0) {
            ps.setInt(6, p.getCategoryId());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("barcode"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity"),
                rs.getInt("min_stock_level"),
                rs.getInt("category_id"),
                rs.getString("category_name")
        );
    }

    /** Turns a raw SQLException into a message a non-developer user can actually read. */
    private String describe(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("barcode")) {
                return "That barcode is already used by another product.";
            }
            return "This action conflicts with existing data (constraint violation).";
        }
        String sqlState = e.getSQLState();
        if ("08001".equals(sqlState) || "08S01".equals(sqlState)) {
            return "Could not reach the MySQL server. Check it is running and the connection URL/port are correct.";
        }
        if ("28000".equals(sqlState)) {
            return "MySQL rejected the username/password in DatabaseConnection.java.";
        }
        if ("42000".equals(sqlState)) {
            return "Database or table not found. Run sql/products_schema.sql first.";
        }
        return e.getMessage();
    }
}
