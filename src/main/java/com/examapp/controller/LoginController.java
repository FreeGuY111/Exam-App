package com.examapp.controller;

import com.examapp.model.User;
import com.examapp.service.AuthService;
import com.examapp.service.SessionManager;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import com.examapp.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Login screen with tab switching between Login and Register.
 */
public class LoginController {

    private BorderPane root;
    private StackPane cardPane;
    private VBox loginForm;
    private VBox registerForm;

    // Login fields
    private TextField loginUsername;
    private PasswordField loginPassword;
    private ToggleGroup roleGroup;
    private RadioButton adminRadio;
    private RadioButton studentRadio;
    private Label loginError;

    // Register fields
    private TextField regUsername;
    private TextField regFullName;
    private TextField regEmail;
    private PasswordField regPassword;
    private PasswordField regConfirm;
    private Label regError;

    public LoginController() {
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0d47a1, #1976d2, #42a5f5);");

        // Left brand panel
        VBox brandPanel = buildBrandPanel();
        root.setLeft(brandPanel);

        // Right: card container
        StackPane rightPanel = new StackPane();
        rightPanel.setPadding(new Insets(40));
        rightPanel.setAlignment(Pos.CENTER);

        cardPane = new StackPane();
        cardPane.getStyleClass().add("login-card");
        cardPane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 8);"
        );
        cardPane.setMaxWidth(430);
        cardPane.setMaxHeight(700);

        // Tab selector
        HBox tabBar = buildTabBar();
        loginForm = buildLoginForm();
        registerForm = buildRegisterForm();
        registerForm.setVisible(false);
        registerForm.setManaged(false);

        VBox cardContent = new VBox(0);
        cardContent.setPadding(new Insets(36, 40, 40, 40));
        cardContent.getChildren().addAll(tabBar, loginForm, registerForm);

        cardPane.getChildren().add(cardContent);
        rightPanel.getChildren().add(cardPane);
        root.setCenter(rightPanel);

        AnimationUtil.slideInFromRight(cardPane, 400);
    }

    private VBox buildBrandPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(340);
        panel.setPadding(new Insets(60, 40, 60, 60));

        Label logo = new Label("E");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        logo.setTextFill(Color.WHITE);
        logo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 16 24 16 24;"
        );

        Label title = new Label("ExamPro");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Online Examination System");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#bbdefb"));
        subtitle.setWrapText(true);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");

        Label feature1 = makeFeatureLabel("Secure Role-based Login");
        Label feature2 = makeFeatureLabel("Timer-based Exam Engine");
        Label feature3 = makeFeatureLabel("MCQ + Subjective Questions");
        Label feature4 = makeFeatureLabel("Instant Result Analytics");

        panel.getChildren().addAll(logo, title, subtitle, sep, feature1, feature2, feature3, feature4);
        return panel;
    }

    private Label makeFeatureLabel(String text) {
        Label lbl = new Label("  " + text);
        lbl.setFont(Font.font("Arial", 14));
        lbl.setTextFill(Color.web("#e3f2fd"));
        lbl.setStyle("-fx-padding: 6 0;");
        return lbl;
    }

    private HBox buildTabBar() {
        HBox tabBar = new HBox(0);
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setStyle("-fx-border-color: #e3e8f0; -fx-border-width: 0 0 2 0;");
        tabBar.setSpacing(0);
        tabBar.setPadding(new Insets(0, 0, 20, 0));

        Button loginTab = new Button("Login");
        Button registerTab = new Button("Register");

        loginTab.setPrefWidth(175);
        registerTab.setPrefWidth(175);
        loginTab.setPrefHeight(40);
        registerTab.setPrefHeight(40);

        styleActiveTab(loginTab);
        styleInactiveTab(registerTab);

        loginTab.setOnAction(e -> {
            showLogin();
            styleActiveTab(loginTab);
            styleInactiveTab(registerTab);
        });
        registerTab.setOnAction(e -> {
            showRegister();
            styleActiveTab(registerTab);
            styleInactiveTab(loginTab);
        });

        tabBar.getChildren().addAll(loginTab, registerTab);
        return tabBar;
    }

    private void styleActiveTab(Button btn) {
        btn.setStyle(
                "-fx-background-color: #1565c0;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8 8 0 0;" +
                        "-fx-cursor: hand;"
        );
    }

    private void styleInactiveTab(Button btn) {
        btn.setStyle(
                "-fx-background-color: #f5f7fa;" +
                        "-fx-text-fill: #666;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8 8 0 0;" +
                        "-fx-cursor: hand;"
        );
    }

    private VBox buildLoginForm() {
        VBox form = new VBox(16);
        form.setPadding(new Insets(20, 0, 0, 0));

        Label heading = new Label("Welcome Back");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        heading.setTextFill(Color.web("#1a237e"));

        Label subheading = new Label("Sign in to your account");
        subheading.setFont(Font.font("Arial", 14));
        subheading.setTextFill(Color.web("#78909c"));

        // Role selection
        Label roleLabel = new Label("Login as:");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        roleLabel.setTextFill(Color.web("#37474f"));

        roleGroup = new ToggleGroup();
        studentRadio = new RadioButton("Student");
        adminRadio = new RadioButton("Administrator");
        studentRadio.setToggleGroup(roleGroup);
        adminRadio.setToggleGroup(roleGroup);
        studentRadio.setSelected(true);
        studentRadio.setStyle("-fx-font-size: 13;");
        adminRadio.setStyle("-fx-font-size: 13;");

        HBox roleBox = new HBox(24, studentRadio, adminRadio);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        roleBox.setPadding(new Insets(4, 12, 4, 12));
        roleBox.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 8;");

        loginUsername = styledField("Username");
        loginPassword = new PasswordField();
        loginPassword.setPromptText("Password");
        loginPassword.getStyleClass().add("styled-field");
        styleInput(loginPassword);

        loginError = new Label("");
        loginError.setTextFill(Color.web("#c62828"));
        loginError.setFont(Font.font("Arial", 13));
        loginError.setWrapText(true);

        Button loginBtn = new Button("Sign In");
        styleActionButton(loginBtn, "#1565c0", "#0d47a1");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        loginPassword.setOnAction(e -> handleLogin());

        form.getChildren().addAll(heading, subheading, roleLabel, roleBox,
                makeFieldLabel("Username"), loginUsername,
                makeFieldLabel("Password"), loginPassword,
                loginError, loginBtn);
        return form;
    }

    private VBox buildRegisterForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20, 0, 0, 0));

        Label heading = new Label("Create Account");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        heading.setTextFill(Color.web("#1a237e"));

        Label subheading = new Label("Join as a student to take exams");
        subheading.setFont(Font.font("Arial", 14));
        subheading.setTextFill(Color.web("#78909c"));

        regUsername = styledField("e.g. john_doe");
        regFullName = styledField("e.g. John Doe");
        regEmail = styledField("e.g. john@email.com");
        regPassword = new PasswordField();
        regPassword.setPromptText("Minimum 6 characters");
        styleInput(regPassword);
        regConfirm = new PasswordField();
        regConfirm.setPromptText("Re-enter password");
        styleInput(regConfirm);

        regError = new Label("");
        regError.setTextFill(Color.web("#c62828"));
        regError.setFont(Font.font("Arial", 13));
        regError.setWrapText(true);

        Button registerBtn = new Button("Create Account");
        styleActionButton(registerBtn, "#2e7d32", "#1b5e20");
        registerBtn.setPrefWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> handleRegister());

        form.getChildren().addAll(heading, subheading,
                makeFieldLabel("Username"), regUsername,
                makeFieldLabel("Full Name"), regFullName,
                makeFieldLabel("Email"), regEmail,
                makeFieldLabel("Password"), regPassword,
                makeFieldLabel("Confirm Password"), regConfirm,
                regError, registerBtn);
        return form;
    }

    private TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        styleInput(field);
        return field;
    }

    private void styleInput(Control field) {
        field.setStyle(
                "-fx-background-color: #f5f7fb;" +
                        "-fx-border-color: #cfd8e3;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 14;" +
                        "-fx-font-size: 14;" +
                        "-fx-pref-height: 42;"
        );
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(field.getStyle().replace("-fx-border-color: #cfd8e3;", "-fx-border-color: #1565c0;"));
            } else {
                field.setStyle(field.getStyle().replace("-fx-border-color: #1565c0;", "-fx-border-color: #cfd8e3;"));
            }
        });
    }

    private Label makeFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#455a64"));
        lbl.setPadding(new Insets(4, 0, 0, 0));
        return lbl;
    }

    private void styleActionButton(Button btn, String color, String hoverColor) {
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 12 0;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(color, hoverColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverColor, color)));
    }

    private void showLogin() {
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginError.setText("");
        AnimationUtil.fadeIn(loginForm, 200);
    }

    private void showRegister() {
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        regError.setText("");
        AnimationUtil.fadeIn(registerForm, 200);
    }

    private void handleLogin() {
        loginError.setText("");
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();
        String role = adminRadio.isSelected() ? Constants.ROLE_ADMIN : Constants.ROLE_STUDENT;

        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please enter username and password.");
            AnimationUtil.shakeNode(cardPane);
            return;
        }

        User user = AuthService.getInstance().login(username, password, role);
        if (user == null) {
            loginError.setText("Invalid username, password, or role. Please try again.");
            AnimationUtil.shakeNode(cardPane);
            loginPassword.clear();
            return;
        }

        SessionManager.getInstance().login(user);

        if (user.isAdmin()) {
            SceneManager.getInstance().showAdminDashboard();
        } else {
            SceneManager.getInstance().showStudentDashboard();
        }
    }

    private void handleRegister() {
        regError.setText("");
        String error = AuthService.getInstance().registerStudent(
                regUsername.getText(),
                regPassword.getText(),
                regConfirm.getText(),
                regFullName.getText(),
                regEmail.getText()
        );
        if (error != null) {
            regError.setText(error);
            AnimationUtil.shakeNode(cardPane);
            return;
        }
        AlertHelper.showInfo("Registration Successful",
                "Account created! You can now log in as a Student.");
        showLogin();
        loginUsername.setText(regUsername.getText());
    }

    public BorderPane getView() {
        return root;
    }
}