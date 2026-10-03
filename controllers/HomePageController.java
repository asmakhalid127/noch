package com.noch.controllers;

import com.noch.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML private Label headerUserLabel;
    @FXML private GridPane productGrid;

    private User currentUser;

    private static final String IMG = "src/main/resources/images/";

    private static final String[][] PRODUCTS = {
        {"1", "NOCH SATIN EVENING DRESS", "£142.00", "Elegant emerald satin evening dress with a sophisticated silhouette. Deep V-back, floor-length cut.", IMG + "product1.jpg"},
        {"2", "NOCH LINEN SHIRT",          "£59.99",  "Breathable pure linen shirt with a loose, relaxed silhouette. Perfect for all seasons.",              IMG + "product2.jpg"},
        {"3", "NOCH SLIM TROUSERS",        "£69.99",  "Tailored slim-fit trousers in stretch cotton. Smart enough for the office, easy enough for every day.", IMG + "product3.jpg"},
        {"4", "NOCH WIDE LEG JEANS",       "£74.99",  "Wide-leg denim in a clean indigo wash. High waist, straight cut, timeless.",                           IMG + "product4.jpg"},
        {"5", "NOCH OVERSHIRT JACKET",     "£89.99",  "A structured overshirt jacket in brushed cotton twill. Wear open or buttoned as a light layer.",        IMG + "product5.jpg"},
        {"6", "NOCH WOOL COAT",            "£149.99", "A minimalist single-breasted wool blend coat. Longline cut, clean finish, cold weather essential.",     IMG + "product6.jpg"},
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProducts();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (headerUserLabel != null) {
            headerUserLabel.setText(user.getName());
        }
    }

    private void loadProducts() {
        int col = 0, row = 0;
        for (String[] product : PRODUCTS) {
            VBox card = createProductCard(product[0], product[1], product[2], product[3], product[4]);
            productGrid.add(card, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }
    }

    private VBox createProductCard(String id, String name, String price, String description, String imagePath) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;");

        // Image
        VBox imgBox = new VBox();
        imgBox.setPrefHeight(220);
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setStyle("-fx-background-color:#f9fafb;");

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            try {
                ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                iv.setFitWidth(280);
                iv.setFitHeight(220);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                imgBox.getChildren().add(iv);
            } catch (Exception e) {
                imgBox.getChildren().add(fallbackLabel(name));
            }
        } else {
            imgBox.getChildren().add(fallbackLabel(name));
        }

        // Info
        VBox info = new VBox(8);
        info.setPadding(new Insets(16));

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-letter-spacing:2px;");
        nameLabel.setWrapText(true);

        HBox stars = new HBox(2);
        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            star.setStyle(i < 4
                ? "-fx-text-fill:black;-fx-font-size:13px;"
                : "-fx-text-fill:#d1d5db;-fx-font-size:13px;");
            stars.getChildren().add(star);
        }

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label viewLabel = new Label("VIEW PRODUCT");
        viewLabel.setStyle("-fx-font-size:11px;-fx-underline:true;-fx-letter-spacing:1px;");
        footer.getChildren().addAll(priceLabel, spacer, viewLabel);

        info.getChildren().addAll(nameLabel, stars, descLabel, footer);
        card.getChildren().addAll(imgBox, info);

        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color:black;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;"));
        card.setOnMouseExited(e ->  card.setStyle("-fx-border-color:#d1d5db;-fx-border-width:1;-fx-background-color:white;-fx-cursor:hand;"));
        card.setOnMouseClicked(e -> navigateToProduct(id));

        return card;
    }

    private Label fallbackLabel(String name) {
        Label l = new Label(String.valueOf(name.charAt(0)));
        l.setStyle("-fx-font-size:48px;-fx-text-fill:#d1d5db;");
        return l;
    }

    private void navigateToProduct(String productId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductPage.fxml"));
            Parent root = loader.load();
            ProductPageController controller = loader.getController();
            controller.setProduct(productId, currentUser);
            Stage stage = (Stage) productGrid.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("NOCH – Product");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) productGrid.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("NOCH – Customer Login");
        } catch (IOException e) { e.printStackTrace(); }
    }
}