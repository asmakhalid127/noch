package com.noch.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:noch_db.sqlite";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                name TEXT NOT NULL,
                role TEXT NOT NULL,
                verified INTEGER DEFAULT 0
            );
        """;

        String insertCustomer =
            "INSERT OR IGNORE INTO users VALUES " +
            "('user-1','user@email.com','user123','USER','CUSTOMER',1)";
        String insertAdmin =
            "INSERT OR IGNORE INTO users VALUES " +
            "('admin-1','admin@noch.com','admin123','ADMIN','ADMIN',1)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(insertCustomer);
            stmt.execute(insertAdmin);
            System.out.println("Database initialised.");
        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    public static String validateUser(String email, String pass) {
        String sql = "SELECT role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role").toUpperCase();
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }
}