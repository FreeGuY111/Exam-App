package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.model.ExamResult;
import com.examapp.model.Question;
import com.examapp.service.ResultService;
import com.examapp.service.SessionManager;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Result screen displayed after exam submission.
 * Shows score, percentage, pass/fail, answer review, and export option.
 */
public class ResultController {

    private BorderPane root;
    private final ExamResult result;
    private final Exam exam;

    public ResultController(ExamResult result, Exam exam) {
        this.result = result;
        this.exam = exam;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");

        // Top bar
        root.setTop(buildTopBar());

        // Center: scrollable result content
        ScrollPane scroll = new ScrollPane(buildResultContent());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f0f4ff; -fx-background-color: #f0f4ff;");
        root.setCenter(scroll);
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(60);
        bar.setStyle("-fx-background-color: #1565c0; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");

        Label title = new Label("Exam Result  —  " + exam.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button dashBtn = new Button("Go to Dashboard");
        dashBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        dashBtn.setOnAction(e -> {
            if (SessionManager.getInstance().isAdmin()) {
                SceneManager.getInstance().showAdminDashboard();
            } else {
                SceneManager.getInstance().showStudentDashboard();
            }
        });

        bar.getChildren().addAll(title, spacer, dashBtn);
        return bar;
    }

    private VBox buildResultContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 48, 48, 48));
        content.setAlignment(Pos.TOP_CENTER);

        // Hero result card
        VBox heroCard = buildHeroCard();
        content.getChildren().add(heroCard);

        // Stats row
        HBox statsRow = buildStatsRow();
        content.getChildren().add(statsRow);

        // Auto-submit notice
        if (result.isAutoSubmitted()) {
            Label autoNote = new Label("Note: This exam was auto-submitted when the timer expired.");
            autoNote.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            autoNote.setTextFill(Color.web("#e65100"));
            autoNote.setPadding(new Insets(10, 20, 10, 20));
            autoNote.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 8;");
            content.getChildren().add(autoNote);
        }

        // Action buttons
        HBox actions = buildActions();
        content.getChildren().add(actions);

        // Answer review section
        Label reviewTitle = new Label("Answer Review");
        reviewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        reviewTitle.setTextFill(Color.web("#1a237e"));
        content.getChildren().add(reviewTitle);

        // Answer cards
        List<Question> questions = exam.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            content.getChildren().add(buildAnswerReviewCard(questions.get(i), i + 1));
        }

        AnimationUtil.fadeIn(content, 400);
        return content;
    }

    private VBox buildHeroCard() {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 60, 40, 60));
        card.setMaxWidth(700);
        card.setStyle(
                "-fx-background-color: " + (result.isPassed() ?
                        "linear-gradient(to bottom right, #1b5e20, #2e7d32, #388e3c)" :
                        "linear-gradient(to bottom right, #b71c1c, #c62828, #d32f2f)") + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 24, 0, 0, 6);"
        );

        // Score arc / donut
        StackPane donut = buildScoreDonut();

        Label resultBadge = new Label(result.isPassed() ? "CONGRATULATIONS!" : "BETTER LUCK NEXT TIME");
        resultBadge.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        resultBadge.setTextFill(Color.WHITE);

        Label examTitleLabel = new Label(exam.getTitle());
        examTitleLabel.setFont(Font.font("Arial", 15));
        examTitleLabel.setTextFill(Color.web("rgba(255,255,255,0.85)"));

        String dateStr = result.getSubmittedAt() != null
                ? result.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"))
                : "N/A";
        Label dateLabel = new Label("Submitted: " + dateStr);
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setTextFill(Color.web("rgba(255,255,255,0.7)"));

        Label studentLabel = new Label(result.getStudentFullName() + "  (@" + result.getStudentUsername() + ")");
        studentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        studentLabel.setTextFill(Color.web("rgba(255,255,255,0.9)"));

        card.getChildren().addAll(donut, resultBadge, examTitleLabel, studentLabel, dateLabel);
        return card;
    }

    private StackPane buildScoreDonut() {
        StackPane stack = new StackPane();
        stack.setPrefSize(160, 160);
        stack.setAlignment(Pos.CENTER);

        Circle bg = new Circle(75);
        bg.setFill(Color.web("rgba(0,0,0,0.25)"));
        bg.setStroke(Color.TRANSPARENT);

        double pct = result.getPercentage() / 100.0;

        // Background arc (full ring)
        Arc bgArc = new Arc(80, 80, 64, 64, 90, -360);
        bgArc.setType(ArcType.OPEN);
        bgArc.setFill(Color.TRANSPARENT);
        bgArc.setStroke(Color.web("rgba(255,255,255,0.2)"));
        bgArc.setStrokeWidth(12);
        bgArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Score arc (animated)
        Arc scoreArc = new Arc(80, 80, 64, 64, 90, 0);
        scoreArc.setType(ArcType.OPEN);
        scoreArc.setFill(Color.TRANSPARENT);
        scoreArc.setStroke(Color.WHITE);
        scoreArc.setStrokeWidth(12);
        scoreArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Center text
        VBox centerText = new VBox(2);
        centerText.setAlignment(Pos.CENTER);
        Label pctLabel = new Label("0%");
        pctLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        pctLabel.setTextFill(Color.WHITE);
        pctLabel.setTextAlignment(TextAlignment.CENTER);
        Label scoreText = new Label(result.getFinalScore() + "/" + result.getTotalMarks());
        scoreText.setFont(Font.font("Arial", 13));
        scoreText.setTextFill(Color.web("rgba(255,255,255,0.85)"));
        centerText.getChildren().addAll(pctLabel, scoreText);

        // We embed arcs in a Pane for absolute positioning
        Pane arcPane = new Pane();
        arcPane.setPrefSize(160, 160);
        arcPane.getChildren().addAll(bgArc, scoreArc);

        bgArc.setCenterX(80);
        bgArc.setCenterY(80);
        bgArc.setRadiusX(64);
        bgArc.setRadiusY(64);
        scoreArc.setCenterX(80);
        scoreArc.setCenterY(80);
        scoreArc.setRadiusX(64);
        scoreArc.setRadiusY(64);

        stack.getChildren().addAll(bg, arcPane, centerText);

        // Animate arc and percentage counter
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(scoreArc.lengthProperty(), -360 * pct))
        );
        timeline.play();

        // Animate percentage label
        Timeline pctTimeline = new Timeline();
        pctTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1200), ae -> {
            pctLabel.setText(String.format("%.0f%%", result.getPercentage()));
        }));
        // Intermediate frames
        for (int i = 0; i <= 24; i++) {
            final int frame = i;
            pctTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(frame * 50), ae -> {
                double progress = (frame * 50.0) / 1200.0;
                pctLabel.setText(String.format("%.0f%%", result.getPercentage() * progress));
            }));
        }
        pctTimeline.play();

        return stack;
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.setMaxWidth(700);

        row.getChildren().addAll(
                statBox("MCQ Score", result.getMcqScore() + " / " + result.getTotalMCQMarks(), "#1565c0"),
                statBox("Total Score", result.getFinalScore() + " / " + result.getTotalMarks(),
                        result.isPassed() ? "#2e7d32" : "#c62828"),
                statBox("Questions", String.valueOf(result.getTotalQuestions()), "#6a1b9a"),
                statBox("Time Taken", result.getFormattedTime(), "#e65100"),
                statBox("Status", result.isPassed() ? "PASS" : "FAIL",
                        result.isPassed() ? "#2e7d32" : "#c62828")
        );

        return row;
    }

    private VBox statBox(String label, String value, String color) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 2);"
        );
        HBox.setHgrow(box, Priority.ALWAYS);
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        val.setTextFill(Color.web(color));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web("#78909c"));
        box.getChildren().addAll(val, lbl);
        return box;
    }

    private HBox buildActions() {
        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);
        actions.setMaxWidth(700);

        Button dashBtn = new Button("Go to Dashboard");
        dashBtn.setPrefHeight(44);
        dashBtn.setPrefWidth(200);
        dashBtn.setStyle(
                "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 14;" +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        dashBtn.setOnMouseEntered(e -> dashBtn.setStyle(dashBtn.getStyle().replace("#1565c0", "#0d47a1")));
        dashBtn.setOnMouseExited(e -> dashBtn.setStyle(dashBtn.getStyle().replace("#0d47a1", "#1565c0")));
        dashBtn.setOnAction(e -> {
            if (SessionManager.getInstance().isAdmin()) {
                SceneManager.getInstance().showAdminDashboard();
            } else {
                SceneManager.getInstance().showStudentDashboard();
            }
        });

        Button exportBtn = new Button("Export Result");
        exportBtn.setPrefHeight(44);
        exportBtn.setPrefWidth(180);
        exportBtn.setStyle(
                "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14;" +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle(exportBtn.getStyle().replace("#2e7d32", "#1b5e20")));
        exportBtn.setOnMouseExited(e -> exportBtn.setStyle(exportBtn.getStyle().replace("#1b5e20", "#2e7d32")));
        exportBtn.setOnAction(e -> {
            String path = ResultService.getInstance().exportResult(result);
            if (path != null) {
                AlertHelper.showInfo("Result Exported", "Your result has been saved to:\n" + path);
            } else {
                AlertHelper.showError("Export Failed", "Could not save result file. Check permissions.");
            }
        });

        Button logoutBtn = new Button("Logout");
        logoutBtn.setPrefHeight(44);
        logoutBtn.setPrefWidth(140);
        logoutBtn.setStyle(
                "-fx-background-color: #78909c; -fx-text-fill: white; -fx-font-size: 14;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.getInstance().showLogin();
        });

        actions.getChildren().addAll(dashBtn, exportBtn, logoutBtn);
        return actions;
    }

    private VBox buildAnswerReviewCard(Question q, int index) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setMaxWidth(Double.MAX_VALUE);

        String studentAnswer = result.getAnswers().getOrDefault(q.getId(), "");
        boolean isCorrect = q.isMCQ() && q.checkAnswer(studentAnswer);
        boolean notAnswered = studentAnswer == null || studentAnswer.isBlank();

        String borderColor;
        String bgColor;
        if (q.isSubjective()) {
            borderColor = "#90a4ae";
            bgColor = "white";
        } else if (notAnswered) {
            borderColor = "#bdbdbd";
            bgColor = "#fafafa";
        } else if (isCorrect) {
            borderColor = "#43a047";
            bgColor = "#f1f8e9";
        } else {
            borderColor = "#e53935";
            bgColor = "#fff5f5";
        }

        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 1);"
        );

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label numLbl = new Label("Q" + index);
        numLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        numLbl.setTextFill(Color.web("#1565c0"));
        numLbl.setMinWidth(30);

        String typeColor = q.isMCQ() ? "#0277bd" : "#6a1b9a";
        String typeBg = q.isMCQ() ? "#e1f5fe" : "#f3e5f5";
        Label typeBadge = new Label(q.isMCQ() ? "MCQ" : "Subjective");
        typeBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        typeBadge.setTextFill(Color.web(typeColor));
        typeBadge.setPadding(new Insets(2, 10, 2, 10));
        typeBadge.setStyle("-fx-background-color: " + typeBg + "; -fx-background-radius: 20;");

        Label marksLbl = new Label(q.getMarks() + " mark(s)");
        marksLbl.setFont(Font.font("Arial", 12));
        marksLbl.setTextFill(Color.web("#78909c"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge;
        if (q.isSubjective()) {
            statusBadge = makeBadge("Manual Review", "#546e7a", "#eceff1");
        } else if (notAnswered) {
            statusBadge = makeBadge("Not Answered", "#757575", "#f5f5f5");
        } else if (isCorrect) {
            statusBadge = makeBadge("Correct  +" + q.getMarks(), "#2e7d32", "#e8f5e9");
        } else {
            statusBadge = makeBadge("Wrong  +0", "#c62828", "#ffebee");
        }

        header.getChildren().addAll(numLbl, typeBadge, marksLbl, spacer, statusBadge);

        // Question text
        Label qTextLabel = new Label(q.getQuestionText());
        qTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        qTextLabel.setTextFill(Color.web("#263238"));
        qTextLabel.setWrapText(true);

        card.getChildren().addAll(header, qTextLabel);

        if (q.isMCQ()) {
            // Show options with correct/wrong highlighting
            VBox optionsBox = new VBox(6);
            String[] letters = {"A", "B", "C", "D"};
            List<String> opts = q.getOptions();
            for (int i = 0; i < opts.size(); i++) {
                String letter = letters[i];
                boolean isCorrectOption = letter.equals(q.getCorrectAnswer());
                boolean isStudentChoice = letter.equals(studentAnswer);

                HBox optRow = new HBox(12);
                optRow.setAlignment(Pos.CENTER_LEFT);
                optRow.setPadding(new Insets(8, 14, 8, 14));

                String rowBg;
                String textColor;
                String letterColor;

                if (isCorrectOption && isStudentChoice) {
                    rowBg = "#e8f5e9";
                    textColor = "#1b5e20";
                    letterColor = "#2e7d32";
                } else if (isCorrectOption) {
                    rowBg = "#f1f8e9";
                    textColor = "#2e7d32";
                    letterColor = "#43a047";
                } else if (isStudentChoice) {
                    rowBg = "#ffebee";
                    textColor = "#b71c1c";
                    letterColor = "#ef5350";
                } else {
                    rowBg = "transparent";
                    textColor = "#546e7a";
                    letterColor = "#90a4ae";
                }

                optRow.setStyle("-fx-background-color: " + rowBg + "; -fx-background-radius: 7;");

                Label lCircle = new Label(letter);
                lCircle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                lCircle.setTextFill(Color.web(letterColor));
                lCircle.setMinWidth(24);
                lCircle.setMinHeight(24);
                lCircle.setAlignment(Pos.CENTER);

                Label optText = new Label(opts.get(i));
                optText.setFont(Font.font("Arial", isCorrectOption || isStudentChoice ? FontWeight.BOLD : FontWeight.NORMAL, 13));
                optText.setTextFill(Color.web(textColor));
                optText.setWrapText(true);

                // Suffix markers
                Label marker = new Label("");
                if (isCorrectOption && isStudentChoice) {
                    marker.setText("  Correct Answer (Your choice)");
                    marker.setTextFill(Color.web("#2e7d32"));
                } else if (isCorrectOption) {
                    marker.setText("  Correct Answer");
                    marker.setTextFill(Color.web("#43a047"));
                } else if (isStudentChoice) {
                    marker.setText("  Your Choice");
                    marker.setTextFill(Color.web("#ef5350"));
                }
                marker.setFont(Font.font("Arial", FontWeight.BOLD, 11));

                optRow.getChildren().addAll(lCircle, optText, marker);
                optionsBox.getChildren().add(optRow);
            }
            card.getChildren().add(optionsBox);

        } else {
            // Subjective: show student answer
            String savedSubj = result.getSubjectiveAnswers().getOrDefault(q.getId(),
                    result.getAnswers().getOrDefault(q.getId(), ""));
            Label answerHeader = new Label("Your Answer:");
            answerHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            answerHeader.setTextFill(Color.web("#546e7a"));

            TextArea answerText = new TextArea(savedSubj.isBlank() ? "(Not answered)" : savedSubj);
            answerText.setEditable(false);
            answerText.setWrapText(true);
            answerText.setPrefRowCount(4);
            answerText.setStyle(
                    "-fx-background-color: #f8f9fe; -fx-border-color: #cfd8e3;" +
                            "-fx-border-width: 1; -fx-border-radius: 7; -fx-background-radius: 7;" +
                            "-fx-padding: 8; -fx-font-size: 13;"
            );

            Label manualNote = new Label("Subjective answers require manual grading by the instructor.");
            manualNote.setFont(Font.font("Arial", 11));
            manualNote.setTextFill(Color.web("#90a4ae"));

            card.getChildren().addAll(answerHeader, answerText, manualNote);
        }

        return card;
    }

    private Label makeBadge(String text, String textColor, String bgColor) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        badge.setTextFill(Color.web(textColor));
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 20;");
        return badge;
    }

    public BorderPane getView() {
        return root;
    }
}