package com.noch.dao.impl;

import com.noch.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl {

    // review array:
    // {0:review_id, 1:customer_name, 2:comment, 3:rating, 4:helpful, 5:flagged,
    //  6:user_id, 7:product_id, 8:created_at, 9:editable_until, 10:image_path, 11:unhelpful}

    public List<String[]> getReviewsByProductId(String productId) {
        List<String[]> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reviews.add(new String[]{
                    rs.getString("review_id"),
                    rs.getString("customer_name"),
                    rs.getString("comment"),
                    String.valueOf(rs.getInt("rating")),
                    String.valueOf(rs.getInt("helpful")),
                    String.valueOf(rs.getInt("flagged")),
                    rs.getString("user_id"),
                    rs.getString("product_id"),
                    rs.getString("created_at"),
                    rs.getString("editable_until"),
                    rs.getString("image_path"),
                    String.valueOf(rs.getInt("unhelpful"))
                });
            }
        } catch (SQLException e) {
            System.err.println("ReviewDAO getReviewsByProductId error: " + e.getMessage());
        }
        return reviews;
    }

    public List<String[]> getAllReviews() {
        List<String[]> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reviews.add(new String[]{
                    rs.getString("review_id"),
                    rs.getString("customer_name"),
                    rs.getString("comment"),
                    String.valueOf(rs.getInt("rating")),
                    String.valueOf(rs.getInt("helpful")),
                    String.valueOf(rs.getInt("flagged")),
                    rs.getString("user_id"),
                    rs.getString("product_id"),
                    rs.getString("created_at"),
                    rs.getString("editable_until"),
                    rs.getString("image_path"),
                    String.valueOf(rs.getInt("unhelpful"))
                });
            }
        } catch (SQLException e) {
            System.err.println("ReviewDAO getAllReviews error: " + e.getMessage());
        }
        return reviews;
    }

    public void addReview(String reviewId, String productId, String userId,
                          String customerName, int rating, String comment,
                          String imagePath, String editableUntil) {
        String sql = "INSERT INTO reviews " +
                     "(review_id, product_id, user_id, customer_name, rating, comment, image_path, editable_until) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            stmt.setString(2, productId);
            stmt.setString(3, userId);
            stmt.setString(4, customerName);
            stmt.setInt(5, rating);
            stmt.setString(6, comment);
            stmt.setString(7, imagePath);
            stmt.setString(8, editableUntil);
            stmt.executeUpdate();
            System.out.println("Review saved: " + reviewId);
        } catch (SQLException e) {
            System.err.println("ReviewDAO addReview error: " + e.getMessage());
        }
    }

    public void updateReview(String reviewId, int rating, String comment) {
        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setString(3, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO updateReview error: " + e.getMessage());
        }
    }

    public void updateReviewWithImage(String reviewId, int rating, String comment, String imagePath) {
        String sql = "UPDATE reviews SET rating = ?, comment = ?, image_path = ? WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setString(3, imagePath);
            stmt.setString(4, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO updateReviewWithImage error: " + e.getMessage());
        }
    }

    public void deleteReview(String reviewId) {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM review_votes WHERE review_id = ?")) {
                s.setString(1, reviewId);
                s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM reviews WHERE review_id = ?")) {
                s.setString(1, reviewId);
                s.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("ReviewDAO deleteReview error: " + e.getMessage());
        }
    }

    public void setFlagged(String reviewId, boolean flagged) {
        String sql = "UPDATE reviews SET flagged = ? WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, flagged ? 1 : 0);
            stmt.setString(2, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO setFlagged error: " + e.getMessage());
        }
    }

    public void incrementHelpful(String reviewId) {
        String sql = "UPDATE reviews SET helpful = helpful + 1 WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO incrementHelpful error: " + e.getMessage());
        }
    }

    public void decrementHelpful(String reviewId) {
        String sql = "UPDATE reviews SET helpful = MAX(0, helpful - 1) WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO decrementHelpful error: " + e.getMessage());
        }
    }

    public void incrementUnhelpful(String reviewId) {
        String sql = "UPDATE reviews SET unhelpful = unhelpful + 1 WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO incrementUnhelpful error: " + e.getMessage());
        }
    }

    public void decrementUnhelpful(String reviewId) {
        String sql = "UPDATE reviews SET unhelpful = MAX(0, unhelpful - 1) WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO decrementUnhelpful error: " + e.getMessage());
        }
    }

    public String getVote(String userId, String reviewId) {
        String sql = "SELECT vote_type FROM review_votes WHERE user_id = ? AND review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, reviewId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("vote_type");
        } catch (SQLException e) {
            System.err.println("ReviewDAO getVote error: " + e.getMessage());
        }
        return null;
    }

    public void setVote(String userId, String reviewId, String voteType) {
        String sql = "INSERT OR REPLACE INTO review_votes (user_id, review_id, vote_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, reviewId);
            stmt.setString(3, voteType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO setVote error: " + e.getMessage());
        }
    }

    public void removeVote(String userId, String reviewId) {
        String sql = "DELETE FROM review_votes WHERE user_id = ? AND review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, reviewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDAO removeVote error: " + e.getMessage());
        }
    }
}