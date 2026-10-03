package com.noch.service;

import com.noch.dao.impl.ReviewDAOImpl;

import java.util.List;

public class ReviewService {

    private final ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

    public List<String[]> getReviewsByProductId(String productId) {
        return reviewDAO.getReviewsByProductId(productId);
    }

    public List<String[]> getAllReviews() {
        return reviewDAO.getAllReviews();
    }

    public void addReview(String reviewId, String productId, String userId,
                          String customerName, int rating, String comment,
                          String imagePath, String editableUntil) {
        reviewDAO.addReview(reviewId, productId, userId, customerName,
                            rating, comment, imagePath, editableUntil);
    }

    public void updateReview(String reviewId, int rating, String comment) {
        reviewDAO.updateReview(reviewId, rating, comment);
    }

    public void updateReviewWithImage(String reviewId, int rating, String comment, String imagePath) {
        reviewDAO.updateReviewWithImage(reviewId, rating, comment, imagePath);
    }

    public void deleteReview(String reviewId) {
        reviewDAO.deleteReview(reviewId);
    }

    public void setFlagged(String reviewId, boolean flagged) {
        reviewDAO.setFlagged(reviewId, flagged);
    }

    public void castVote(String userId, String reviewId, String voteType) {
        String existing = reviewDAO.getVote(userId, reviewId);
        if (existing == null) {
            reviewDAO.setVote(userId, reviewId, voteType);
            if ("like".equals(voteType)) reviewDAO.incrementHelpful(reviewId);
            else                          reviewDAO.incrementUnhelpful(reviewId);
        } else if (existing.equals(voteType)) {
            reviewDAO.removeVote(userId, reviewId);
            if ("like".equals(voteType)) reviewDAO.decrementHelpful(reviewId);
            else                          reviewDAO.decrementUnhelpful(reviewId);
        } else {
            reviewDAO.setVote(userId, reviewId, voteType);
            if ("like".equals(voteType)) {
                reviewDAO.incrementHelpful(reviewId);
                reviewDAO.decrementUnhelpful(reviewId);
            } else {
                reviewDAO.incrementUnhelpful(reviewId);
                reviewDAO.decrementHelpful(reviewId);
            }
        }
    }

    public String getVote(String userId, String reviewId) {
        return reviewDAO.getVote(userId, reviewId);
    }
}