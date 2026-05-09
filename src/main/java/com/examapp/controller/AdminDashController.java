package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.service.ExamService;
import com.examapp.service.ResultService;
import com.examapp.service.SessionManager;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import com.examapp.ui.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin dashboard with sidebar navigation and dynamic content area.
 */
public class AdminDashController {

    private BorderPane root;
    private StackPane contentArea;
    private List<Button> navButtons;
    private Button activeNavBtn;

    public AdminDashController() {
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");

        // Top header bar
        HBox topBar = buildTopBar();
        root.setTop(topBar);

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(28));
        contentArea.setAlignment(Pos.TOP_LEFT);
        root.setCenter(contentArea);

        // Show overview by default
        showOverview();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 0));
        bar.setPrefHeight(60);
        bar.setStyle("-fx-background-color: #1565c0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

        Label logo = new Label("  ExamPro  Admin");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logo.setTextFill(Color.WHITE);
        logo.setPadding(new Insets(0, 0, 0, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeBtn = new Button(ThemeManager.getThemeLabel());
        styleTopBarButton(themeBtn);
        themeBtn.setOnAction(e -> {
            ThemeManager.toggleTheme();
            themeBtn.setText(ThemeManager.getThemeLabel());
        });

        String adminName = SessionManager.getInstance().getCurrentUser().getFullName();
        Label userLabel = new Label("  " + adminName + " (Admin)");
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        userLabel.setTextFill(Color.web("#bbdefb"));

        bar.getChildren().addAll(logo, spacer, themeBtn, userLabel);
        return bar;
    }

    private void styleTopBarButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 14;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("0.15", "0.28")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("0.28", "0.15")));
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(24, 12, 24, 12));
        sidebar.setStyle("-fx-background-color: #0d47a1;");

        Label menuLabel = new Label("MAIN MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.web("#90caf9"));
        menuLabel.setPadding(new Insets(0, 0, 10, 10));

        Button overviewBtn = navButton("  Dashboard", "Overview");
        Button createTestBtn = navButton("  Create Test", "CreateTest");
        Button manageExamsBtn = navButton("  Manage Exams", "ManageExams");
        Button viewResultsBtn = navButton("  View Results", "ViewResults");

        Label settingsLabel = new Label("SETTINGS");
        settingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        settingsLabel.setTextFill(Color.web("#90caf9"));
        settingsLabel.setPadding(new Insets(16, 0, 10, 10));

        Button changePassBtn = navButton("  Change Password", "ChangePass");
        Button logoutBtn = navButton("  Logout", "Logout");
        logoutBtn.setStyle(logoutBtn.getStyle() + "-fx-text-fill: #ef9a9a;");

        navButtons = List.of(overviewBtn, createTestBtn, manageExamsBtn, viewResultsBtn, changePassBtn, logoutBtn);

        overviewBtn.setOnAction(e -> { setActive(overviewBtn); showOverview(); });
        createTestBtn.setOnAction(e -> { SceneManager.getInstance().showCreateExam(); });
        manageExamsBtn.setOnAction(e -> { setActive(manageExamsBtn); showManageExams(); });
        viewResultsBtn.setOnAction(e -> { SceneManager.getInstance().showViewResults(); });
        changePassBtn.setOnAction(e -> { SceneManager.getInstance().showChangePassword(); });
        logoutBtn.setOnAction(e -> handleLogout());

        setActive(overviewBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("v1.0.0");
        versionLabel.setFont(Font.font("Arial", 11));
        versionLabel.setTextFill(Color.web("#5585cc"));
        versionLabel.setPadding(new Insets(0, 0, 0, 10));

        sidebar.getChildren().addAll(menuLabel, overviewBtn, createTestBtn, manageExamsBtn, viewResultsBtn,
                settingsLabel, changePassBtn, spacer, logoutBtn, versionLabel);
        return sidebar;
    }

    private Button navButton(String text, String tag) {
        Button btn = new Button(text);
        btn.setPrefWidth(196);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(42);
        btn.setUserData(tag);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #b3cde8;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            if (btn != activeNavBtn) {
                btn.setStyle(btn.getStyle().replace("-fx-background-color: transparent;", "-fx-background-color: rgba(255,255,255,0.08);"));
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeNavBtn) {
                btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.08);", "-fx-background-color: transparent;"));
            }
        });
        return btn;
    }

    private void setActive(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #b3cde8;" +
                            "-fx-font-size: 14;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }
        activeNavBtn = btn;
        btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
    }

    private void showOverview() {
        contentArea.getChildren().clear();

        VBox overview = new VBox(20);
        overview.setAlignment(Pos.TOP_LEFT);

        Label title = sectionTitle("Admin Dashboard");
        Label sub = new Label("Welcome, " + SessionManager.getInstance().getCurrentUser().getFullName());
        sub.setFont(Font.font("Arial", 15));
        sub.setTextFill(Color.web("#546e7a"));

        // Stats row
        List<Exam> allExams = ExamService.getInstance().getAllExams();
        int totalResults = ResultService.getInstance().getAllResults().size();
        long activeExams = allExams.stream().filter(Exam::isActive).count();

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                statCard("Total Exams", String.valueOf(allExams.size()), "#1565c0"),
                statCard("Active Exams", String.valueOf(activeExams), "#2e7d32"),
                statCard("Total Attempts", String.valueOf(totalResults), "#e65100"),
                statCard("Registered Students", countStudents(), "#6a1b9a")
        );

        // Recent exams table
        Label recentLabel = sectionTitle("Recent Exams");
        VBox examList = buildExamListCards(allExams);

        overview.getChildren().addAll(title, sub, statsRow, recentLabel, examList);
        contentArea.getChildren().add(overview);
        AnimationUtil.fadeIn(overview, 250);
    }

    private String countStudents() {
        try {
            long count = com.examapp.util.FileManager.loadUsers().stream()
                    .filter(u -> "STUDENT".equals(u.getRole())).count();
            return String.valueOf(count);
        } catch (Exception e) {
            return "0";
        }
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(160);
        card.setPrefHeight(90);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);" +
                        "-fx-padding: 16;"
        );

        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valLabel.setTextFill(Color.web(color));

        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("Arial", 12));
        nameLabel.setTextFill(Color.web("#78909c"));

        card.getChildren().addAll(valLabel, nameLabel);
        return card;
    }

    private VBox buildExamListCards(List<Exam> exams) {
        VBox list = new VBox(10);
        if (exams.isEmpty()) {
            Label empty = new Label("No exams created yet. Click 'Create Test' to add one.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.web("#90a4ae"));
            list.getChildren().add(empty);
            return list;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        for (Exam exam : exams) {
            HBox card = new HBox(16);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(14, 18, 14, 18));
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 1);"
            );

            VBox info = new VBox(4);
            Label examName = new Label(exam.getTitle());
            examName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            examName.setTextFill(Color.web("#1a237e"));

            String qCount = exam.getQuestions().size() + " Questions  |  " + exam.getDurationMinutes() + " min";
            Label meta = new Label(qCount + "  |  Created: " + (exam.getCreatedAt() != null ? exam.getCreatedAt().format(fmt) : "N/A"));
            meta.setFont(Font.font("Arial", 12));
            meta.setTextFill(Color.web("#607d8b"));

            info.getChildren().addAll(examName, meta);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label statusBadge = new Label(exam.isActive() ? "Active" : "Inactive");
            statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            statusBadge.setTextFill(exam.isActive() ? Color.web("#2e7d32") : Color.web("#b71c1c"));
            statusBadge.setPadding(new Insets(4, 12, 4, 12));
            statusBadge.setStyle(
                    "-fx-background-color: " + (exam.isActive() ? "#e8f5e9" : "#ffebee") + ";" +
                            "-fx-background-radius: 20;"
            );

            Button manageBtn = new Button("Manage Questions");
            manageBtn.setStyle(
                    "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 12;" +
                            "-fx-background-radius: 7; -fx-padding: 6 12; -fx-cursor: hand;"
            );
            manageBtn.setOnAction(e -> SceneManager.getInstance().showManageQuestions(exam));

            card.getChildren().addAll(info, spacer, statusBadge, manageBtn);
            list.getChildren().add(card);
        }
        return list;
    }

    private void showManageExams() {
        contentArea.getChildren().clear();
        VBox page = new VBox(16);
        page.setAlignment(Pos.TOP_LEFT);

        Label title = sectionTitle("Manage Exams");
        List<Exam> exams = ExamService.getInstance().getAllExams();

        if (exams.isEmpty()) {
            Label empty = new Label("No exams found. Create one first.");
            empty.setTextFill(Color.web("#90a4ae"));
            page.getChildren().addAll(title, empty);
        } else {
            page.getChildren().add(title);
            for (Exam exam : exams) {
                HBox card = new HBox(12);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(14, 18, 14, 18));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 1);");

                VBox info = new VBox(3);
                Label name = new Label(exam.getTitle());
                name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                name.setTextFill(Color.web("#1a237e"));
                Label meta = new Label(exam.getQuestions().size() + " questions | " + exam.getDurationMinutes() + " min");
                meta.setFont(Font.font("Arial", 12));
                meta.setTextFill(Color.web("#607d8b"));
                info.getChildren().addAll(name, meta);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button toggleBtn = new Button(exam.isActive() ? "Deactivate" : "Activate");
                toggleBtn.setStyle("-fx-background-color: " + (exam.isActive() ? "#f57f17" : "#2e7d32") +
                        "; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 6 12; -fx-cursor: hand;");
                toggleBtn.setOnAction(e -> {
                    ExamService.getInstance().toggleExamActive(exam.getId());
                    showManageExams();
                });

                Button questionsBtn = new Button("Questions");
                questionsBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 6 12; -fx-cursor: hand;");
                questionsBtn.setOnAction(e -> SceneManager.getInstance().showManageQuestions(exam));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 6 12; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    if (AlertHelper.showConfirm("Delete Exam",
                            "Are you sure you want to delete '" + exam.getTitle() + "'? This cannot be undone.")) {
                        ExamService.getInstance().deleteExam(exam.getId());
                        showManageExams();
                    }
                });

                card.getChildren().addAll(info, spacer, toggleBtn, questionsBtn, deleteBtn);
                page.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
        AnimationUtil.fadeIn(page, 250);
    }

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.web("#1a237e"));
        return lbl;
    }

    private void handleLogout() {
        if (AlertHelper.showConfirm("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            SceneManager.getInstance().showLogin();
        }
    }

    public BorderPane getView() {
        return root;
    }
}