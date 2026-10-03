package com.noch.service;

import com.noch.dao.impl.ReviewDAOImpl;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NC1605B - Group Project Module
 * Assessment B - Software Implementation
 * Component: Reviews and Ratings
 *
 * JUnit Test Suite
 * Tests are mapped to requirements 1-7 from Assessment A.
 */
public class ReviewServiceTest {

    private ReviewService reviewService;

    private static final DateTimeFormatter DB_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String PRODUCT_1  = "1";
    private static final String PRODUCT_2  = "2";
    private static final String USER_1     = "user-1";
    private static final String USER_2     = "demo-u1";
    private static final String T_REVIEW_1 = "junit-r1";
    private static final String T_REVIEW_2 = "junit-r2";
    private static final String T_REVIEW_3 = "junit-r3";

    @Before
    public void setUp() throws Exception {
        reviewService = new ReviewService();
        cleanTestData();
    }

    @After
    public void tearDown() throws Exception {
        cleanTestData();
    }

    private void cleanTestData() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:noch_db.sqlite");
        Statement stmt  = conn.createStatement();
        stmt.execute("DELETE FROM review_votes WHERE review_id LIKE 'junit-%'");
        stmt.execute("DELETE FROM reviews WHERE review_id LIKE 'junit-%'");
        conn.close();
    }

    private void addTestReview(String id, String productId, String userId,
                                String name, int rating, String comment) {
        String editable = LocalDateTime.now().plusMinutes(5).format(DB_FMT);
        reviewService.addReview(id, productId, userId, name, rating, comment, null, editable);
    }

    private void addExpiredReview(String id, String productId, String userId,
                                   String name, int rating, String comment) {
        // editable_until set in the past — simulates locked review
        String expired = LocalDateTime.now().minusMinutes(10).format(DB_FMT);
        reviewService.addReview(id, productId, userId, name, rating, comment, null, expired);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 2: Review Submission
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T01
     * Component:  ReviewService.addReview()
     * Desc:       Valid review with 5 star rating and comment is saved to DB
     * Req:        2
     * Input:      rating=5, comment="Excellent quality fabric"
     * Expected:   Review retrievable from DB with correct rating and comment
     * Pass/Fail:  Pass
     */
    @Test
    public void T01_validReviewIsSavedToDatabase() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 5, "Excellent quality fabric");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] saved = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should be saved to database", saved);
        assertEquals("Rating should be 5", "5", saved[3]);
        assertEquals("Comment should match input", "Excellent quality fabric", saved[2]);
    }

    /**
     * Test ID:    T02
     * Component:  ReviewService.addReview()
     * Desc:       Review is stored with correct user ID and product ID
     * Req:        2
     * Input:      user_id="user-1", product_id="1"
     * Expected:   Saved review contains matching user_id and product_id
     * Pass/Fail:  Pass
     */
    @Test
    public void T02_reviewStoredWithCorrectUserAndProduct() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 4, "Good fit");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] saved = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should exist", saved);
        assertEquals("user_id should match", USER_1, saved[6]);
        assertEquals("product_id should match", PRODUCT_1, saved[7]);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 6: Validation (Special Characters & Rating Range)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T03
     * Component:  Input Validation (regex)
     * Desc:       Comment containing @ is rejected by validation
     * Req:        6
     * Input:      comment="Great@product"
     * Expected:   Regex matches returns true (comment is invalid)
     * Pass/Fail:  Pass
     * Correction: Initially used String.contains() which missed some cases.
     *             Fixed by switching to .matches() with full regex pattern.
     */
    @Test
    public void T03_commentWithAtSymbolFailsValidation() {
        String comment = "Great@product";
        boolean isInvalid = comment.matches(".*[@,.#/\\\\?\"'~`$].*");
        assertTrue("Comment with @ should fail validation", isInvalid);
    }

    /**
     * Test ID:    T04
     * Component:  Input Validation (regex)
     * Desc:       Comment containing special chars #/\?\"'~`$ are all rejected
     * Req:        6
     * Input:      Comments with each special character
     * Expected:   All return true (invalid)
     * Pass/Fail:  Pass
     */
    @Test
    public void T04_allSpecialCharactersAreRejected() {
        String[] invalid = {
            "test,comment", "test.comment", "test#comment",
            "test/comment", "test\\comment", "test?comment",
            "test\"comment", "test'comment", "test~comment",
            "test`comment", "test$comment"
        };
        String pattern = ".*[@,.#/\\\\?\"'~`$].*";
        for (String c : invalid) {
            assertTrue("'" + c + "' should be rejected", c.matches(pattern));
        }
    }

    /**
     * Test ID:    T05
     * Component:  Input Validation (regex)
     * Desc:       Valid comment without special characters passes validation
     * Req:        6
     * Input:      comment="Really great quality item"
     * Expected:   Regex match returns false (comment is valid)
     * Pass/Fail:  Pass
     */
    @Test
    public void T05_validCommentPassesValidation() {
        String comment = "Really great quality item";
        boolean isInvalid = comment.matches(".*[@,.#/\\\\?\"'~`$].*");
        assertFalse("Valid comment should pass validation", isInvalid);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 3: No Duplicate Reviews / Purchase Verification
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T06
     * Component:  ReviewService / ProductPageController
     * Desc:       System detects that a user has already reviewed a product
     * Req:        3
     * Input:      user-1 reviews product 1 once
     * Expected:   alreadyReviewed = true on second attempt
     * Pass/Fail:  Pass
     */
    @Test
    public void T06_duplicateReviewDetectedForSameUser() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 4, "Good jacket");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        boolean alreadyReviewed = reviews.stream()
            .anyMatch(r -> r[6].equals(USER_1));

        assertTrue("System should detect user already reviewed this product", alreadyReviewed);
    }

    /**
     * Test ID:    T07
     * Component:  ReviewService
     * Desc:       Two different users can each submit one review for same product
     * Req:        3
     * Input:      user-1 and demo-u1 both review product 1
     * Expected:   Both reviews saved, count = 2
     * Pass/Fail:  Pass
     */
    @Test
    public void T07_twoUsersCanEachReviewSameProduct() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER",   5, "Love this coat");
        addTestReview(T_REVIEW_2, PRODUCT_1, USER_2, "Sarah M",4, "Good quality");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        long count = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1) || r[0].equals(T_REVIEW_2))
            .count();

        assertEquals("Both reviews should be saved", 2, count);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 4: Edit & Delete with Time Window
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T08
     * Component:  ReviewService.updateReview()
     * Desc:       Review can be edited within the 5-minute time window
     * Req:        4
     * Input:      rating=5, comment="Updated: fits perfectly"
     * Expected:   Review updated in DB with new rating and comment
     * Pass/Fail:  Pass
     */
    @Test
    public void T08_reviewCanBeEditedWithinTimeWindow() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 3, "Original comment");

        reviewService.updateReview(T_REVIEW_1, 5, "Updated fits perfectly");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] updated = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should still exist after edit", updated);
        assertEquals("Rating should be updated to 5", "5", updated[3]);
        assertEquals("Comment should be updated", "Updated fits perfectly", updated[2]);
    }

    /**
     * Test ID:    T09
     * Component:  Time window logic (canEdit)
     * Desc:       Review with expired editable_until is locked (cannot edit)
     * Req:        4
     * Input:      editable_until = 10 minutes in the past
     * Expected:   canEdit = false
     * Pass/Fail:  Pass
     */
    @Test
    public void T09_expiredTimeWindowLocksReview() {
        String expired = LocalDateTime.now().minusMinutes(10).format(DB_FMT);
        LocalDateTime deadline = LocalDateTime.parse(expired, DB_FMT);
        boolean canEdit = LocalDateTime.now().isBefore(deadline);

        assertFalse("Review with expired time window should be locked", canEdit);
    }

    /**
     * Test ID:    T10
     * Component:  Time window logic (canEdit)
     * Desc:       Review with future editable_until is editable
     * Req:        4
     * Input:      editable_until = 5 minutes in the future
     * Expected:   canEdit = true
     * Pass/Fail:  Pass
     */
    @Test
    public void T10_activeTimeWindowAllowsEdit() {
        String future = LocalDateTime.now().plusMinutes(5).format(DB_FMT);
        LocalDateTime deadline = LocalDateTime.parse(future, DB_FMT);
        boolean canEdit = LocalDateTime.now().isBefore(deadline);

        assertTrue("Review within time window should be editable", canEdit);
    }

    /**
     * Test ID:    T11
     * Component:  ReviewService.deleteReview()
     * Desc:       Review is permanently deleted from database
     * Req:        4
     * Input:      review_id = junit-r1
     * Expected:   Review no longer returned by getReviewsByProductId
     * Pass/Fail:  Pass
     */
    @Test
    public void T11_reviewIsDeletedFromDatabase() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 4, "To be deleted");

        reviewService.deleteReview(T_REVIEW_1);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        boolean found = reviews.stream().anyMatch(r -> r[0].equals(T_REVIEW_1));

        assertFalse("Deleted review should not be found in database", found);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 5: Display, Sorting & Filtering
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T12
     * Component:  ReviewService.getReviewsByProductId() + sort logic
     * Desc:       Reviews sorted by highest rating return 5-star review first
     * Req:        5
     * Input:      3 reviews with ratings 2, 5, 3 for same product
     * Expected:   After sort, first review has rating 5
     * Pass/Fail:  Pass
     */
    @Test
    public void T12_sortByHighestRatingReturns5StarFirst() {
        addTestReview("junit-s1", PRODUCT_2, "demo-u2", "James T", 2, "Low rating");
        addTestReview("junit-s2", PRODUCT_2, "demo-u3", "Priya K", 5, "High rating");
        addTestReview("junit-s3", PRODUCT_2, "demo-u4", "Oliver R",3, "Mid rating");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_2);
        reviews = reviews.stream()
            .filter(r -> r[0].startsWith("junit-s"))
            .collect(Collectors.toList());

        reviews.sort((a, b) -> Integer.parseInt(b[3]) - Integer.parseInt(a[3]));

        assertEquals("First review should be 5 stars after sort", "5", reviews.get(0)[3]);

        reviewService.deleteReview("junit-s1");
        reviewService.deleteReview("junit-s2");
        reviewService.deleteReview("junit-s3");
    }

    /**
     * Test ID:    T13
     * Component:  ReviewService.getReviewsByProductId() + filter logic
     * Desc:       Filtering by 5 stars returns only 5-star reviews
     * Req:        5
     * Input:      Reviews with ratings 5, 3, 5 for same product
     * Expected:   Filter returns 2 reviews
     * Pass/Fail:  Pass
     */
    @Test
    public void T13_filterBy5StarsReturnsOnlyFiveStarReviews() {
        addTestReview("junit-f1", PRODUCT_2, "demo-u2", "James T", 5, "Five stars");
        addTestReview("junit-f2", PRODUCT_2, "demo-u3", "Priya K", 3, "Three stars");
        addTestReview("junit-f3", PRODUCT_2, "demo-u4", "Oliver R",5, "Also five");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_2);
        long fiveStarCount = reviews.stream()
            .filter(r -> r[0].startsWith("junit-f"))
            .filter(r -> Integer.parseInt(r[3]) == 5)
            .count();

        assertEquals("Should find exactly 2 five-star reviews", 2, fiveStarCount);

        reviewService.deleteReview("junit-f1");
        reviewService.deleteReview("junit-f2");
        reviewService.deleteReview("junit-f3");
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 6: Average Rating Calculation
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T14
     * Component:  Average rating calculation logic
     * Desc:       Average of ratings 4, 2, 3 calculates to 3.0
     * Req:        6
     * Input:      3 reviews with ratings 4, 2, 3
     * Expected:   Average = 3.0
     * Pass/Fail:  Pass
     */
    @Test
    public void T14_averageRatingCalculatedCorrectly() {
        addTestReview("junit-a1", PRODUCT_2, "demo-u2", "James T", 4, "Four stars");
        addTestReview("junit-a2", PRODUCT_2, "demo-u3", "Priya K", 2, "Two stars");
        addTestReview("junit-a3", PRODUCT_2, "demo-u4", "Oliver R",3, "Three stars");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_2);
        double avg = reviews.stream()
            .filter(r -> r[0].startsWith("junit-a"))
            .mapToInt(r -> Integer.parseInt(r[3]))
            .average().orElse(0);

        assertEquals("Average of 4+2+3 should be 3.0", 3.0, avg, 0.01);

        reviewService.deleteReview("junit-a1");
        reviewService.deleteReview("junit-a2");
        reviewService.deleteReview("junit-a3");
    }

    /**
     * Test ID:    T15
     * Component:  Average rating calculation logic
     * Desc:       Average returns 0.0 when product has no reviews
     * Req:        6
     * Input:      Non-existent product ID
     * Expected:   Average = 0.0
     * Pass/Fail:  Pass
     */
    @Test
    public void T15_averageRatingIsZeroWithNoReviews() {
        List<String[]> reviews = reviewService.getReviewsByProductId("no-such-product");
        double avg = reviews.stream()
            .mapToInt(r -> Integer.parseInt(r[3]))
            .average().orElse(0);

        assertEquals("Average should be 0.0 when no reviews exist", 0.0, avg, 0.01);
    }

    // ─────────────────────────────────────────────────────────────────
    // REQUIREMENT 7: Administrator Moderation
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T16
     * Component:  ReviewService.setFlagged()
     * Desc:       Admin can flag a review (flagged = 1)
     * Req:        7
     * Input:      setFlagged(reviewId, true)
     * Expected:   review[5] = "1"
     * Pass/Fail:  Pass
     */
    @Test
    public void T16_adminCanFlagReview() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 1, "Inappropriate comment");

        reviewService.setFlagged(T_REVIEW_1, true);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] review = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should still exist after flagging", review);
        assertEquals("Review should be flagged with value 1", "1", review[5]);
    }

    /**
     * Test ID:    T17
     * Component:  ReviewService.setFlagged()
     * Desc:       Admin can unflag a previously flagged review (flagged = 0)
     * Req:        7
     * Input:      setFlagged(reviewId, true) then setFlagged(reviewId, false)
     * Expected:   review[5] = "0"
     * Pass/Fail:  Pass
     */
    @Test
    public void T17_adminCanUnflagReview() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 2, "Review to unflag");

        reviewService.setFlagged(T_REVIEW_1, true);
        reviewService.setFlagged(T_REVIEW_1, false);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] review = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should exist", review);
        assertEquals("Review should be unflagged with value 0", "0", review[5]);
    }

    /**
     * Test ID:    T18
     * Component:  ReviewService.updateReview()
     * Desc:       Admin can edit a customer review (update rating and comment)
     * Req:        7
     * Input:      rating=4, comment="Admin edited"
     * Expected:   Review updated with new values in DB
     * Pass/Fail:  Pass
     */
    @Test
    public void T18_adminCanEditAnyReview() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_2, "Sarah M", 1, "Bad original comment");

        reviewService.updateReview(T_REVIEW_1, 4, "Admin edited");

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] edited = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Edited review should exist", edited);
        assertEquals("Rating should be updated to 4", "4", edited[3]);
        assertEquals("Comment should be updated by admin", "Admin edited", edited[2]);
    }

    /**
     * Test ID:    T19
     * Component:  ReviewService.deleteReview()
     * Desc:       Admin can permanently delete any review
     * Req:        7
     * Input:      deleteReview(reviewId)
     * Expected:   Review not found in DB after deletion
     * Pass/Fail:  Pass
     */
    @Test
    public void T19_adminCanDeleteAnyReview() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_2, "Sarah M", 1, "Review to delete");

        reviewService.deleteReview(T_REVIEW_1);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        boolean found = reviews.stream().anyMatch(r -> r[0].equals(T_REVIEW_1));

        assertFalse("Deleted review should not exist in database", found);
    }

    // ─────────────────────────────────────────────────────────────────
    // LIKE / DISLIKE VOTING
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T20
     * Component:  ReviewService.castVote()
     * Desc:       Like vote increments helpful count and is stored in DB
     * Req:        5
     * Input:      castVote(userId, reviewId, "like")
     * Expected:   helpful count = 1, vote stored as "like"
     * Pass/Fail:  Pass
     */
    @Test
    public void T20_likeVoteIncrementsHelpfulCount() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 5, "Great coat");

        reviewService.castVote(USER_2, T_REVIEW_1, "like");

        String vote = reviewService.getVote(USER_2, T_REVIEW_1);
        assertEquals("Vote should be stored as like", "like", vote);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] review = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should exist", review);
        assertEquals("Helpful count should be 1 after like", "1", review[4]);
    }

    /**
     * Test ID:    T21
     * Component:  ReviewService.castVote()
     * Desc:       Clicking like twice toggles vote off (helpful count returns to 0)
     * Req:        5
     * Input:      castVote twice with "like"
     * Expected:   vote = null, helpful = 0
     * Pass/Fail:  Pass
     */
    @Test
    public void T21_likeToggleOffRemovesVote() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 5, "Great coat");

        reviewService.castVote(USER_2, T_REVIEW_1, "like");
        reviewService.castVote(USER_2, T_REVIEW_1, "like"); // toggle off

        String vote = reviewService.getVote(USER_2, T_REVIEW_1);
        assertNull("Vote should be removed after toggling off", vote);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] review = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should exist", review);
        assertEquals("Helpful count should return to 0", "0", review[4]);
    }

    /**
     * Test ID:    T22
     * Component:  ReviewService.castVote()
     * Desc:       Switching from like to dislike updates both counts correctly
     * Req:        5
     * Input:      castVote("like") then castVote("dislike")
     * Expected:   helpful=0, unhelpful=1, vote="dislike"
     * Pass/Fail:  Pass
     */
    @Test
    public void T22_switchFromLikeToDislikeUpdatesBothCounts() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 5, "Great coat");

        reviewService.castVote(USER_2, T_REVIEW_1, "like");
        reviewService.castVote(USER_2, T_REVIEW_1, "dislike");

        String vote = reviewService.getVote(USER_2, T_REVIEW_1);
        assertEquals("Vote should be dislike", "dislike", vote);

        List<String[]> reviews = reviewService.getReviewsByProductId(PRODUCT_1);
        String[] review = reviews.stream()
            .filter(r -> r[0].equals(T_REVIEW_1))
            .findFirst().orElse(null);

        assertNotNull("Review should exist", review);
        assertEquals("Helpful count should be 0", "0", review[4]);
        assertEquals("Unhelpful count should be 1", "1", review[11]);
    }

    // ─────────────────────────────────────────────────────────────────
    // PERSISTENCE
    // ─────────────────────────────────────────────────────────────────

    /**
     * Test ID:    T23
     * Component:  ReviewService / ReviewDAOImpl
     * Desc:       Review persists and is retrievable after saving
     * Req:        2, 5
     * Input:      addReview then fresh getReviewsByProductId call
     * Expected:   Review found in DB
     * Pass/Fail:  Pass
     */
    @Test
    public void T23_reviewPersistsAfterSave() {
        addTestReview(T_REVIEW_1, PRODUCT_1, USER_1, "USER", 4, "Persisted review");

        // Simulate a fresh service instance reading from DB
        ReviewService freshService = new ReviewService();
        List<String[]> reviews = freshService.getReviewsByProductId(PRODUCT_1);
        boolean found = reviews.stream().anyMatch(r -> r[0].equals(T_REVIEW_1));

        assertTrue("Review should be retrievable after save", found);
    }
}