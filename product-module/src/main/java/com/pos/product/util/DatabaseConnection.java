package com.pos.product.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place to obtain a MySQL JDBC connection.
 *
 * If your existing project already has its own connection class
 * (e.g. shared across Engineer 1's Authentication module), delete this
 * file and point the DAOs in this package at that shared class instead -
 * just make sure it exposes a static getConnection() method.
 */
public class DatabaseConnection {

    // ⚠️ Update these to match your project's MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/pos_system?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j to your dependencies.", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
