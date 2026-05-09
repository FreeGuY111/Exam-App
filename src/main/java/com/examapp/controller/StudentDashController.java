package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.model.ExamResult;
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
 * Student dashboard showing available exams, previous results, and navigation.
 */
public class StudentDashController {

    private BorderPane root;
    private StackPane contentArea;

    public StudentDashController() {
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");

        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(28));
        contentArea.setAlignment(Pos.TOP_LEFT);
        root.setCenter(contentArea);

        showAvailableExams();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 0));
        bar.setPrefHeight(60);
        bar.setStyle("-fx-background-color: #0d47a1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

        Label logo = new Label("  ExamPro  Student Portal");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logo.setTextFill(Color.WHITE);
        logo.setPadding(new Insets(0, 0, 0, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeBtn = new Button(ThemeManager.getThemeLabel());
        themeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        themeBtn.setOnAction(e -> { ThemeManager.toggleTheme(); themeBtn.setText(ThemeManager.getThemeLabel()); });

        String studentName = SessionManager.getInstance().getCurrentUser().getFullName();
        Label userLabel = new Label("  " + studentName);
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        userLabel.setTextFill(Color.web("#bbdefb"));

        bar.getChildren().addAll(logo, spacer, themeBtn, userLabel);
        return bar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(24, 12, 24, 12));
        sidebar.setStyle("-fx-background-color: #0d47a1;");

        Label menuLabel = new Label("NAVIGATION");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        menuLabel.setTextFill(Color.web("#90caf9"));
        menuLabel.setPadding(new Insets(0, 0, 10, 10));

        Button availableBtn = sidebarButton("  Available Tests");
        Button resultsBtn = sidebarButton("  My Results");

        Label settingsLabel = new Label("ACCOUNT");
        settingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        settingsLabel.setTextFill(Color.web("#90caf9"));
        settingsLabel.setPadding(new Insets(16, 0, 10, 10));

        Button changePassBtn = sidebarButton("  Change Password");
        Button logoutBtn = sidebarButton("  Logout");
        logoutBtn.setStyle(logoutBtn.getStyle() + " -fx-text-fill: #ef9a9a;");

        availableBtn.setOnAction(e -> showAvailableExams());
        resultsBtn.setOnAction(e -> showMyResults());
        changePassBtn.setOnAction(e -> SceneManager.getInstance().showChangePassword());
        logoutBtn.setOnAction(e -> {
            if (AlertHelper.showConfirm("Logout", "Are you sure you want to logout?")) {
                SessionManager.getInstance().logout();
                SceneManager.getInstance().showLogin();
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(menuLabel, availableBtn, resultsBtn,
                settingsLabel, changePassBtn, spacer, logoutBtn);
        return sidebar;
    }

    private Button sidebarButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(196);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(42);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3cde8; -fx-font-size: 14; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3cde8; -fx-font-size: 14; -fx-background-radius: 8; -fx-cursor: hand;"));
        return btn;
    }

    private void showAvailableExams() {
        contentArea.getChildren().clear();
        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_LEFT);

        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        String fullName = SessionManager.getInstance().getCurrentUser().getFullName();

        Label title = new Label("Available Tests");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1a237e"));

        Label welcome = new Label("Welcome back, " + fullName + "! Choose a test below to begin.");
        welcome.setFont(Font.font("Arial", 14));
        welcome.setTextFill(Color.web("#546e7a"));

        List<Exam> exams = ExamService.getInstance().getActiveExams();

        if (exams.isEmpty()) {
            Label empty = new Label("No tests are available at the moment. Please check back later.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.web("#90a4ae"));
            page.getChildren().addAll(title, welcome, empty);
        } else {
            page.getChildren().addAll(title, welcome);
            for (Exam exam : exams) {
                page.getChildren().add(buildExamCard(exam, username));
            }
        }

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
        AnimationUtil.fadeIn(page, 250);
    }

    private VBox buildExamCard(Exam exam, String username) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 12, 0, 0, 2);"
        );

        boolean alreadyAttempted = ResultService.getInstance().hasAttempted(username, exam.getId());

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label examTitle = new Label(exam.getTitle());
        examTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        examTitle.setTextFill(Color.web("#1a237e"));

        Label desc = new Label(exam.getDescription() != null ? exam.getDescription() : "");
        desc.setFont(Font.font("Arial", 13));
        desc.setTextFill(Color.web("#607d8b"));
        desc.setWrapText(true);

        info.getChildren().addAll(examTitle, desc);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(alreadyAttempted ? "Completed" : "Available");
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusBadge.setTextFill(alreadyAttempted ? Color.web("#1565c0") : Color.web("#2e7d32"));
        statusBadge.setPadding(new Insets(5, 14, 5, 14));
        statusBadge.setStyle("-fx-background-color: " + (alreadyAttempted ? "#e3f2fd" : "#e8f5e9") +
                "; -fx-background-radius: 20;");

        header.getChildren().addAll(info, spacer, statusBadge);

        // Stats row
        HBox stats = new HBox(24);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                metaStat("Questions", String.valueOf(exam.getQuestions().size())),
                metaStat("Duration", exam.getDurationMinutes() + " min"),
                metaStat("MCQ", String.valueOf(exam.getMCQCount())),
                metaStat("Subjective", String.valueOf(exam.getSubjectiveCount())),
                metaStat("Total Marks", String.valueOf(exam.getTotalMarks()))
        );

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (alreadyAttempted) {
            Label attempted = new Label("You have already completed this exam.");
            attempted.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            attempted.setTextFill(Color.web("#1565c0"));
            actions.getChildren().add(attempted);
        } else {
            Button startBtn = new Button("  Start Exam  ");
            startBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            startBtn.setStyle(
                    "-fx-background-color: #1565c0;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 24;" +
                            "-fx-cursor: hand;"
            );
            startBtn.setOnMouseEntered(e -> startBtn.setStyle(startBtn.getStyle().replace("#1565c0", "#0d47a1")));
            startBtn.setOnMouseExited(e -> startBtn.setStyle(startBtn.getStyle().replace("#0d47a1", "#1565c0")));
            startBtn.setOnAction(e -> {
                if (AlertHelper.showConfirm("Start Exam",
                        "You are about to start:\n'" + exam.getTitle() + "'\n\n" +
                                "Duration: " + exam.getDurationMinutes() + " minutes\n" +
                                "Questions: " + exam.getQuestions().size() + "\n\n" +
                                "Once started, you cannot pause the timer. Ready?")) {
                    SceneManager.getInstance().showExam(exam);
                }
            });
            actions.getChildren().add(startBtn);
        }

        card.getChildren().addAll(header, new Separator(), stats, actions);
        return card;
    }

    private VBox metaStat(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        val.setTextFill(Color.web("#1565c0"));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#90a4ae"));
        box.getChildren().addAll(val, lbl);
        return box;
    }

    private void showMyResults() {
        contentArea.getChildren().clear();
        VBox page = new VBox(16);
        page.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("My Results");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#1a237e"));

        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        List<ExamResult> results = ResultService.getInstance().getStudentResults(username);

        if (results.isEmpty()) {
            Label empty = new Label("You haven't completed any exams yet. Start a test to see your results here.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.web("#90a4ae"));
            page.getChildren().addAll(title, empty);
        } else {
            page.getChildren().add(title);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
            for (ExamResult result : results) {
                page.getChildren().add(buildResultCard(result, fmt));
            }
        }

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
        AnimationUtil.fadeIn(page, 250);
    }

    private HBox buildResultCard(ExamResult result, DateTimeFormatter fmt) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 1);"
        );

        // Score circle
        VBox scoreBox = new VBox(2);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setMinWidth(80);
        Label scoreLabel = new Label(result.getFinalScore() + "/" + result.getTotalMarks());
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        scoreLabel.setTextFill(result.isPassed() ? Color.web("#2e7d32") : Color.web("#c62828"));
        Label pctLabel = new Label(String.format("%.0f%%", result.getPercentage()));
        pctLabel.setFont(Font.font("Arial", 13));
        pctLabel.setTextFill(Color.web("#78909c"));
        scoreBox.getChildren().addAll(scoreLabel, pctLabel);

        VBox info = new VBox(4);
        Label examTitle = new Label(result.getExamTitle());
        examTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        examTitle.setTextFill(Color.web("#1a237e"));

        String dateStr = result.getSubmittedAt() != null ? result.getSubmittedAt().format(fmt) : "N/A";
        Label meta = new Label("Submitted: " + dateStr + "  |  Time: " + result.getFormattedTime());
        meta.setFont(Font.font("Arial", 12));
        meta.setTextFill(Color.web("#78909c"));

        Label badge = new Label(result.isPassed() ? "PASS" : "FAIL");
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        badge.setTextFill(result.isPassed() ? Color.web("#2e7d32") : Color.web("#c62828"));
        badge.setPadding(new Insets(3, 12, 3, 12));
        badge.setStyle("-fx-background-color: " + (result.isPassed() ? "#e8f5e9" : "#ffebee") +
                "; -fx-background-radius: 20;");

        info.getChildren().addAll(examTitle, meta, badge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> {
            String path = ResultService.getInstance().exportResult(result);
            if (path != null) {
                AlertHelper.showInfo("Export Successful", "Result exported to:\n" + path);
            } else {
                AlertHelper.showError("Export Failed", "Could not export result file.");
            }
        });

        card.getChildren().addAll(scoreBox, new Separator(javafx.geometry.Orientation.VERTICAL), info, spacer, exportBtn);
        return card;
    }

    public BorderPane getView() {
        return root;
    }
}