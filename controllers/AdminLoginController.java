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
import java.util.ArrayList;

public class AdminLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        // Database validation
        String role = DatabaseConnection.validateUser(email, password);

        if (role == null) {
            showAlert("Login Failed", "Invalid email or password. Please try again.");
            return;
        }

        if (!role.equals("ADMIN")) {
            showAlert("Login Failed", "This is the admin login. Please use the customer login.");
            return;
        }

        // Build admin user object
        User user = new User(
            "admin-1", email, "ADMIN",
            "admin", true, new ArrayList<>()
        );

        navigateToDashboard(user);
    }

    @FXML
    private void handleAutoFill() {
        emailField.setText("admin@noch.com");
        passwordField.setText("admin123");
    }

    @FXML
    private void goToCustomerLogin() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/CustomerLogin.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("NOCH – Customer Login");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateToDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Dashboard.fxml")
            );
            Parent root = loader.load();
            AdminDashboardController dashController = loader.getController();
            dashController.setUser(user);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("NOCH – Admin Dashboard");
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