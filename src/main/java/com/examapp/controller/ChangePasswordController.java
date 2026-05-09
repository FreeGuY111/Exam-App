package com.examapp.controller;

import com.examapp.service.AuthService;
import com.examapp.service.SessionManager;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Change Password screen for both Admin and Student users.
 */
public class ChangePasswordController {

    private BorderPane root;

    public ChangePasswordController() {
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");

        // Top bar
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 24, 0, 24));
        topBar.setPrefHeight(60);
        topBar.setStyle("-fx-background-color: #1565c0; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");

        Button backBtn = new Button("Back");
        backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> goBack());

        Label pageTitle = new Label("  Change Password");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pageTitle.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(backBtn, pageTitle);
        root.setTop(topBar);

        // Center card
        StackPane center = new StackPane();
        center.setPadding(new Insets(60));
        center.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(20);
        card.setMaxWidth(480);
        card.setPadding(new Insets(40, 44, 44, 44));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 4);"
        );

        // Header
        Label heading = new Label("Update Your Password");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#1a237e"));

        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        Label subheading = new Label("Account: " + username);
        subheading.setFont(Font.font("Arial", 13));
        subheading.setTextFill(Color.web("#78909c"));

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        // Fields
        PasswordField currentPassField = passwordField("Enter your current password");
        PasswordField newPassField = passwordField("Minimum 6 characters");
        PasswordField confirmPassField = passwordField("Re-enter new password");

        // Strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(Double.MAX_VALUE);
        strengthBar.setPrefHeight(6);
        strengthBar.setStyle("-fx-accent: #ef5350; -fx-background-radius: 4; -fx-border-radius: 4;");

        Label strengthLabel = new Label("Password strength: —");
        strengthLabel.setFont(Font.font("Arial", 12));
        strengthLabel.setTextFill(Color.web("#78909c"));

        newPassField.textProperty().addListener((obs, old, val) -> {
            double strength = calculateStrength(val);
            strengthBar.setProgress(strength);
            String color;
            String text;
            if (strength <= 0.25)      { color = "#ef5350"; text = "Weak"; }
            else if (strength <= 0.50) { color = "#ff9800"; text = "Fair"; }
            else if (strength <= 0.75) { color = "#fdd835"; text = "Good"; }
            else                       { color = "#43a047"; text = "Strong"; }
            strengthBar.setStyle("-fx-accent: " + color + "; -fx-background-radius: 4; -fx-border-radius: 4;");
            strengthLabel.setText("Password strength: " + text);
        });

        // Error / success labels
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#c62828"));
        errorLabel.setFont(Font.font("Arial", 13));
        errorLabel.setWrapText(true);

        // Update button
        Button updateBtn = new Button("Update Password");
        updateBtn.setPrefWidth(Double.MAX_VALUE);
        updateBtn.setPrefHeight(46);
        updateBtn.setStyle(
                "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 15;" +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        updateBtn.setOnMouseEntered(e -> updateBtn.setStyle(updateBtn.getStyle().replace("#1565c0", "#0d47a1")));
        updateBtn.setOnMouseExited(e -> updateBtn.setStyle(updateBtn.getStyle().replace("#0d47a1", "#1565c0")));

        updateBtn.setOnAction(e -> {
            errorLabel.setText("");
            String current = currentPassField.getText();
            String newPass = newPassField.getText();
            String confirm = confirmPassField.getText();

            String error = AuthService.getInstance().changePassword(username, current, newPass, confirm);
            if (error != null) {
                errorLabel.setText(error);
                AnimationUtil.shakeNode(card);
                return;
            }

            AlertHelper.showInfo("Password Updated", "Your password has been changed successfully.");
            currentPassField.clear();
            newPassField.clear();
            confirmPassField.clear();
            strengthBar.setProgress(0);
            strengthLabel.setText("Password strength: —");
        });

        card.getChildren().addAll(
                heading,
                subheading,
                sep,
                fieldBlock("Current Password", currentPassField),
                fieldBlock("New Password", newPassField),
                strengthBar,
                strengthLabel,
                fieldBlock("Confirm New Password", confirmPassField),
                errorLabel,
                updateBtn
        );

        center.getChildren().add(card);
        root.setCenter(center);
        AnimationUtil.slideInFromRight(card, 350);
    }

    private double calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        double score = 0;
        if (password.length() >= 6)  score += 0.25;
        if (password.length() >= 10) score += 0.25;
        if (password.matches(".*[A-Z].*")) score += 0.15;
        if (password.matches(".*[0-9].*")) score += 0.15;
        if (password.matches(".*[^a-zA-Z0-9].*")) score += 0.20;
        return Math.min(score, 1.0);
    }

    private PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setStyle(
                "-fx-background-color: #f5f7fb; -fx-border-color: #cfd8e3;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 10 14; -fx-font-size: 14;"
        );
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(field.getStyle().replace("-fx-border-color: #cfd8e3;", "-fx-border-color: #1565c0;"));
            } else {
                field.setStyle(field.getStyle().replace("-fx-border-color: #1565c0;", "-fx-border-color: #cfd8e3;"));
            }
        });
        return field;
    }

    private VBox fieldBlock(String labelText, Control field) {
        VBox block = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#455a64"));
        block.getChildren().addAll(lbl, field);
        return block;
    }

    private void goBack() {
        if (SessionManager.getInstance().isAdmin()) {
            SceneManager.getInstance().showAdminDashboard();
        } else {
            SceneManager.getInstance().showStudentDashboard();
        }
    }

    public BorderPane getView() {
        return root;
    }
}