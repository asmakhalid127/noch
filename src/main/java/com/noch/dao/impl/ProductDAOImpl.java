package com.noch.dao.impl;

import com.noch.dao.ProductDAO;
import com.noch.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<String[]> getAllProducts() {
        List<String[]> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE activity = 'Active'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(new String[]{
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    "£" + String.format("%.2f", rs.getDouble("price")),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            System.err.println("ProductDAO getAllProducts error: " + e.getMessage());
        }
        return products;
    }

    @Override
    public String[] getProductById(String id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    "£" + String.format("%.2f", rs.getDouble("price")),
                    rs.getString("description")
                };
            }
        } catch (SQLException e) {
            System.err.println("ProductDAO getProductById error: " + e.getMessage());
        }
        return null;
    }
}
