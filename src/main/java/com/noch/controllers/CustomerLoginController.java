package com.noch.controllers;

import com.noch.models.User;
import com.noch.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class CustomerLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        if (email.toLowerCase().contains("noch")) {
            showError("Invalid email address.");
            return;
        }

        // Database validation
        String role = DatabaseConnection.validateUser(email, password);

        if (role == null) {
            showAlert("Login Failed", "Invalid email or password. Please try again.");
            return;
        }

        if (!role.equals("CUSTOMER")) {
            showAlert("Login Failed", "This is the customer login. Please use the admin login.");
            return;
        }

        // Build user object
        User user = new User(
            "user-1", email, email.split("@")[0].toUpperCase(),
            "customer", true,
            Arrays.asList("3","4","5","6","7","8","9")
        );

        navigateToHome(user);
    }

    @FXML
    private void handleAutoFill() {
        emailField.setText("user@email.com");
        passwordField.setText("user123");
    }

    @FXML
    private void goToAdminLogin() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/AdminLogin.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("NOCH – Admin Login");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateToHome(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/HomePage.fxml")
            );
            Parent root = loader.load();
            HomePageController homeController = loader.getController();
            homeController.setUser(user);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("NOCH – Home");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}