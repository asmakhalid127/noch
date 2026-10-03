package com.noch.dao.impl;

import com.noch.dao.UserDAO;
import com.noch.db.DBConnection;
import com.noch.models.User;

import java.sql.*;
import java.util.Arrays;
import java.util.ArrayList;

public class UserDAOImpl implements UserDAO {

    @Override
    public User findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role").toLowerCase();
                return new User(
                    rs.getString("user_id"),
                    rs.getString("email"),
                    rs.getString("name"),
                    role,
                    rs.getInt("verified") == 1,
                    role.equals("customer")
                        ? Arrays.asList("1","2","3","4","5","6")
                        : new ArrayList<>()
                );
            }
        } catch (SQLException e) {
            System.err.println("UserDAO error: " + e.getMessage());
        }
        return null;
    }
}