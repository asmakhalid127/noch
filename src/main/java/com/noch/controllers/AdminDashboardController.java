package com.noch.controllers;

import com.noch.models.User;
import com.noch.service.ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalReviewsLabel;
    @FXML private Label flaggedReviewsLabel;
    @FXML private HBox topReviewsBox;
    @FXML private TextField searchField;
    @FXML private GridPane productsGrid;
    @FXML private Label noResultsLabel;
    @FXML private Label headerUserLabel;

    private User currentUser;
    private final ReviewService reviewService = new ReviewService();
    private static final String IMG = "src/main/resources/images/";

    private static final String[][] PRODUCTS = {
        {"1", "NOCH SATIN EVENING DRESS", "£142.00", "Elegant emerald satin evening dress with a sophisticated silhouette.",       IMG + "product1.jpg"},
        {"2", "NOCH LINEN SHIRT",          "£59.99",  "Breathable pure linen shirt with a loose, relaxed silhouette.",              IMG + "product2.jpg"},
        {"3", "NOCH SLIM TROUSERS",        "£69.99",  "Tailored slim-fit trousers in stretch cotton.",                              IMG + "product3.jpg"},
        {"4", "NOCH WIDE LEG JEANS",       "£74.99",  "Wide-leg denim in a clean indigo wash. High waist, straight cut.",           IMG + "product4.jpg"},
        {"5", "NOCH OVERSHIRT JACKET",     "£89.99",  "A structured overshirt jacket in brushed cotton twill.",                     IMG + "product5.jpg"},
        {"6", "NOCH WOOL COAT",            "£149.99", "A minimalist single-breasted wool blend coat. Longline cut.",                IMG + "product6.jpg"},
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateStats();
        loadTopReviews();
        loadProducts(null);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null && headerUserLabel != null) headerUserLabel.setText("ADMIN: " + user.getName());
    }

    private void updateStats() {
        totalProductsLabel.setText(String.valueOf(PRODUCTS.length));
        List<String[]> all = reviewService.getAllReviews();
        totalReviewsLabel.setText(String.valueOf(all.size()));
        long flagged = all.stream().filter(r -> "1".equals(r[5]) || "true".equals(r[5])).count();
        flaggedReviewsLabel.setText(String.valueOf(flagged));
    }

    private void loadTopReviews() {
        topReviewsBox.getChildren().clear();
        List<String[]> top = reviewService.getAllReviews().stream()
            .filter(r -> Integer.parseInt(r[3]) >= 4)
            .sorted((a, b) -> Integer.parseInt(b[3]) - Integer.parseInt(a[3]))
            .limit(3).collect(Collectors.toList());

        if (top.isEmpty()) {
            Label e = new Label("No top reviews yet.");
            e.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");
            topReviewsBox.getChildren().add(e);
            return;
        }
        top.forEach(r -> topReviewsBox.getChildren().add(createTopReviewCard(r)));
    }

    private VBox createTopReviewCard(String[] review) {
        VBox card = new VBox(8);
        card.setPrefWidth(280);
        card.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-padding:16;-fx-background-color:white;");
        HBox stars = new HBox(2);
        int rating = Integer.parseInt(review[3]);
        for (int i=1;i<=5;i++) { Label s=new Label("★"); s.setStyle(i<=rating?"-fx-text-fill:black;-fx-font-size:13px;":"-fx-text-fill:#d1d5db;-fx-font-size:13px;"); stars.getChildren().add(s); }
        Label name = new Label(review[1]); name.setStyle("-fx-font-weight:bold;-fx-font-size:12px;");
        Label product = new Label(getProductName(review[7])); product.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        Label comment = new Label(review[2]); comment.setWrapText(true); comment.setStyle("-fx-font-size:12px;-fx-text-fill:#4b5563;");
        Label helpful = new Label(review[4] + " helpful"); helpful.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");
        card.getChildren().addAll(stars, name, product, comment, helpful);
        return card;
    }

    private String getProductName(String productId) {
        return Arrays.stream(PRODUCTS).filter(p -> p[0].equals(productId)).map(p -> p[1]).findFirst().orElse("Unknown");
    }

    @FXML private void handleSearch() { loadProducts(searchField.getText().trim().toLowerCase()); }

    private void loadProducts(String query) {
        productsGrid.getChildren().clear();
        int col = 0, row = 0;
        List<String[]> filtered = new ArrayList<>();
        for (String[] p : PRODUCTS) {
            if (query == null || query.isEmpty() || p[1].toLowerCase().contains(query) || p[3].toLowerCase().contains(query))
                filtered.add(p);
        }
        noResultsLabel.setVisible(filtered.isEmpty()); noResultsLabel.setManaged(filtered.isEmpty());
        for (String[] product : filtered) {
            productsGrid.add(createProductCard(product), col, row);
            col++; if (col == 3) { col = 0; row++; }
        }
    }

    private VBox createProductCard(String[] product) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;");
        card.setPadding(new Insets(16));

        VBox imgBox = new VBox();
        imgBox.setPrefHeight(160); imgBox.setAlignment(Pos.CENTER);
        imgBox.setStyle("-fx-background-color:#f9fafb;");
        File imgFile = new File(product[4]);
        if (imgFile.exists()) {
            try {
                ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                iv.setFitWidth(240); iv.setFitHeight(160); iv.setPreserveRatio(true); iv.setSmooth(true);
                imgBox.getChildren().add(iv);
            } catch (Exception e) { imgBox.getChildren().add(fallbackLabel(product[1])); }
        } else { imgBox.getChildren().add(fallbackLabel(product[1])); }

        Label nameLabel = new Label(product[1]); nameLabel.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-letter-spacing:1px;"); nameLabel.setWrapText(true);
        Label descLabel = new Label(product[3]); descLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;"); descLabel.setWrapText(true);
        Label priceLabel = new Label(product[2]); priceLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;");

        List<String[]> productReviews = reviewService.getReviewsByProductId(product[0]);
        long flaggedCount = productReviews.stream().filter(r -> "1".equals(r[5]) || "true".equals(r[5])).count();
        Label reviewsLabel = new Label("Reviews: " + productReviews.size() + (flaggedCount > 0 ? "  ⚑ " + flaggedCount + " flagged" : ""));
        reviewsLabel.setStyle("-fx-font-size:12px;-fx-text-fill:" + (flaggedCount > 0 ? "black" : "#6b7280") + ";");

        Label viewLabel = new Label("VIEW REVIEWS →"); viewLabel.setStyle("-fx-font-size:11px;-fx-underline:true;-fx-letter-spacing:1px;");
        card.getChildren().addAll(imgBox, nameLabel, descLabel, priceLabel, reviewsLabel, viewLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color:black;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;"));
        card.setOnMouseExited(e ->  card.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;"));
        card.setOnMouseClicked(e -> navigateToProductReviews(product[0], product[1]));
        return card;
    }

    private Label fallbackLabel(String name) {
        Label l = new Label(String.valueOf(name.charAt(0))); l.setStyle("-fx-font-size:40px;-fx-text-fill:#d1d5db;"); return l;
    }

    private void navigateToProductReviews(String productId, String productName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminProductReviews.fxml"));
            Parent root = loader.load();
            AdminProductReviewsController ctrl = loader.getController();
            ctrl.setProduct(productId, productName); ctrl.setUser(currentUser);
            Stage stage = (Stage) totalProductsLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800)); stage.setTitle("NOCH – " + productName + " Reviews");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalProductsLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700)); stage.setTitle("NOCH – Admin Login");
        } catch (IOException e) { e.printStackTrace(); }
    }
}