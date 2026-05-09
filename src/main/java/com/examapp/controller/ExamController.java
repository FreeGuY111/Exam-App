package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.model.ExamResult;
import com.examapp.model.Question;
import com.examapp.service.ResultService;
import com.examapp.service.SessionManager;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Full exam interface - direct field references, no .lookup() calls.
 * Timer, MCQ cards, subjective textarea, palette navigation, anti-cheat, auto-submit.
 */
public class ExamController {

    private BorderPane root;
    private final Exam exam;
    private final List<Question> questions;
    private int currentIndex = 0;
    private final int totalSeconds;
    private int remainingSeconds;
    private Timeline timer;
    private boolean examSubmitted = false;
    private final long startTimestamp;
    private final Map<String, String> answers = new HashMap<>();

    // Direct field references - no lookup() needed
    private Label timerLabel;
    private Label progressLabel;
    private ProgressBar progressBar;
    private Label questionTextLabel;
    private HBox optionsContainer;
    private TextArea subjectiveArea;
    private ToggleGroup mcqToggleGroup;
    private HBox paletteDotRow;
    private Label questionCounter;
    private Button prevBtn;
    private Button nextBtn;

    // Anti-cheat
    private int focusLossCount = 0;
    private static final int MAX_FOCUS_LOSS = 3;

    public ExamController(Exam exam) {
        this.exam = exam;
        this.questions = exam.getQuestionsForExam();
        this.totalSeconds = exam.getDurationMinutes() * 60;
        this.remainingSeconds = this.totalSeconds;
        this.startTimestamp = System.currentTimeMillis();
        buildUI();
        startTimer();
        setupFocusDetection();
        loadQuestion(0);
    }

    // =========================================================================
    // BUILD UI
    // =========================================================================

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");
        root.setTop(buildTopBar());
        root.setCenter(buildCenterPane());
        root.setBottom(buildBottomBar());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(64);
        bar.setStyle("-fx-background-color: #0d47a1; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.18),8,0,0,3);");

        VBox titleBox = new VBox(2);
        Label examTitle = new Label(exam.getTitle());
        examTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        examTitle.setTextFill(Color.WHITE);
        Label studentName = new Label(SessionManager.getInstance().getCurrentUser().getFullName());
        studentName.setFont(Font.font("Arial", 12));
        studentName.setTextFill(Color.web("#90caf9"));
        titleBox.getChildren().addAll(examTitle, studentName);

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        VBox progressBox = new VBox(4);
        progressBox.setAlignment(Pos.CENTER);
        progressLabel = new Label("Question 1 / " + questions.size());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        progressLabel.setTextFill(Color.web("#e3f2fd"));
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(5);
        progressBar.setStyle("-fx-accent: #42a5f5; -fx-background-radius: 4;");
        progressBox.getChildren().addAll(progressLabel, progressBar);

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        VBox timerBox = new VBox(2);
        timerBox.setAlignment(Pos.CENTER_RIGHT);
        Label timerCaption = new Label("Time Remaining");
        timerCaption.setFont(Font.font("Arial", 11));
        timerCaption.setTextFill(Color.web("#90caf9"));
        timerLabel = new Label(formatTime(remainingSeconds));
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        timerLabel.setTextFill(Color.web("#4fc3f7"));
        timerBox.getChildren().addAll(timerCaption, timerLabel);

        bar.getChildren().addAll(titleBox, sp1, progressBox, sp2, timerBox);
        return bar;
    }

    private ScrollPane buildCenterPane() {
        VBox outer = new VBox(20);
        outer.setPadding(new Insets(24, 40, 24, 40));
        outer.setAlignment(Pos.TOP_LEFT);

        // Palette row
        paletteDotRow = new HBox(6);
        paletteDotRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;
            Button dot = new Button(String.valueOf(i + 1));
            dot.setMinSize(32, 32);
            dot.setMaxSize(32, 32);
            dot.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            dot.setStyle(dotStyle(false, false));
            dot.setOnAction(e -> { saveCurrentAnswer(); loadQuestion(idx); });
            paletteDotRow.getChildren().add(dot);
        }

        // Question card
        VBox questionCard = new VBox(20);
        questionCard.setPadding(new Insets(28, 32, 28, 32));
        questionCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0, 0, 3);"
        );

        questionTextLabel = new Label();
        questionTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        questionTextLabel.setTextFill(Color.web("#1a237e"));
        questionTextLabel.setWrapText(true);
        questionTextLabel.setMaxWidth(Double.MAX_VALUE);

        optionsContainer = new HBox();
        optionsContainer.setAlignment(Pos.TOP_LEFT);

        subjectiveArea = new TextArea();
        subjectiveArea.setPromptText("Type your detailed answer here...");
        subjectiveArea.setPrefRowCount(8);
        subjectiveArea.setWrapText(true);
        subjectiveArea.setStyle(
                "-fx-background-color: #f8f9fe; -fx-border-color: #90caf9;" +
                        "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 12; -fx-font-size: 14;"
        );
        subjectiveArea.setVisible(false);
        subjectiveArea.setManaged(false);

        questionCard.getChildren().addAll(questionTextLabel, optionsContainer, subjectiveArea);
        outer.getChildren().addAll(paletteDotRow, questionCard);

        ScrollPane scroll = new ScrollPane(outer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f0f4ff; -fx-background-color: #f0f4ff;");
        return scroll;
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(16, 32, 16, 32));
        bar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,-2);");

        prevBtn = new Button("Previous");
        prevBtn.setPrefWidth(130);
        prevBtn.setPrefHeight(42);
        prevBtn.setStyle(navBtnStyle(false));
        prevBtn.setDisable(true);
        prevBtn.setOnAction(e -> { saveCurrentAnswer(); if (currentIndex > 0) loadQuestion(currentIndex - 1); });

        questionCounter = new Label("1 / " + questions.size());
        questionCounter.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        questionCounter.setTextFill(Color.web("#546e7a"));

        nextBtn = new Button("Next");
        nextBtn.setPrefWidth(130);
        nextBtn.setPrefHeight(42);
        nextBtn.setStyle(navBtnStyle(true));
        nextBtn.setDisable(questions.size() <= 1);
        nextBtn.setOnAction(e -> { saveCurrentAnswer(); if (currentIndex < questions.size() - 1) loadQuestion(currentIndex + 1); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button submitBtn = new Button("Submit Exam");
        submitBtn.setPrefHeight(42);
        submitBtn.setPrefWidth(150);
        submitBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(submitBtn.getStyle().replace("#c62828", "#b71c1c")));
        submitBtn.setOnMouseExited( e -> submitBtn.setStyle(submitBtn.getStyle().replace("#b71c1c", "#c62828")));
        submitBtn.setOnAction(e -> { saveCurrentAnswer(); confirmAndSubmit(false); });

        bar.getChildren().addAll(prevBtn, questionCounter, nextBtn, spacer, submitBtn);
        return bar;
    }

    // =========================================================================
    // LOAD QUESTION
    // =========================================================================

    private void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;
        Question q = questions.get(index);

        // Update palette dots
        for (int i = 0; i < paletteDotRow.getChildren().size(); i++) {
            Button dot = (Button) paletteDotRow.getChildren().get(i);
            boolean active   = (i == index);
            boolean answered = answers.containsKey(questions.get(i).getId())
                    && !answers.get(questions.get(i).getId()).isBlank();
            dot.setStyle(dotStyle(active, answered));
        }

        // Update progress
        progressLabel.setText("Question " + (index + 1) + " / " + questions.size());
        progressBar.setProgress((double)(index + 1) / questions.size());

        // Update question text
        questionTextLabel.setText((q.isMCQ() ? "[MCQ]  " : "[Subjective]  ") + q.getQuestionText());

        // Update bottom bar
        questionCounter.setText((index + 1) + " / " + questions.size());
        prevBtn.setDisable(index == 0);
        nextBtn.setDisable(index == questions.size() - 1);

        // Show correct input widget
        if (q.isMCQ()) {
            subjectiveArea.setVisible(false);
            subjectiveArea.setManaged(false);
            buildMCQOptions(q);
            optionsContainer.setVisible(true);
            optionsContainer.setManaged(true);
        } else {
            optionsContainer.setVisible(false);
            optionsContainer.setManaged(false);
            subjectiveArea.setText(answers.getOrDefault(q.getId(), ""));
            subjectiveArea.setVisible(true);
            subjectiveArea.setManaged(true);
        }
    }

    private void buildMCQOptions(Question q) {
        optionsContainer.getChildren().clear();

        VBox vbox = new VBox(12);
        vbox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(vbox, Priority.ALWAYS);

        mcqToggleGroup = new ToggleGroup();
        String savedAnswer = answers.getOrDefault(q.getId(), "");
        String[] letters = {"A", "B", "C", "D"};
        List<String> opts = q.getOptions();

        for (int i = 0; i < opts.size(); i++) {
            String letter = letters[i];
            String optText = opts.get(i);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(mcqToggleGroup);
            rb.setUserData(letter);

            HBox card = new HBox(14);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(13, 18, 13, 18));
            card.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(card, Priority.ALWAYS);
            boolean preSelected = letter.equals(savedAnswer);
            card.setStyle(optCardStyle(preSelected));

            Label circle = new Label(letter);
            circle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            circle.setTextFill(Color.web("#1565c0"));
            circle.setMinWidth(28);
            circle.setMinHeight(28);
            circle.setAlignment(Pos.CENTER);
            circle.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 14;");

            Label text = new Label(optText);
            text.setFont(Font.font("Arial", 14));
            text.setTextFill(Color.web("#263238"));
            text.setWrapText(true);
            HBox.setHgrow(text, Priority.ALWAYS);

            card.getChildren().addAll(rb, circle, text);

            if (preSelected) rb.setSelected(true);

            // Click card -> select radio + highlight
            card.setOnMouseClicked(e -> {
                rb.setSelected(true);
                for (Node n : vbox.getChildren()) if (n instanceof HBox) ((HBox) n).setStyle(optCardStyle(false));
                card.setStyle(optCardStyle(true));
            });

            rb.selectedProperty().addListener((obs, was, now) -> {
                if (now) {
                    for (Node n : vbox.getChildren()) if (n instanceof HBox) ((HBox) n).setStyle(optCardStyle(false));
                    card.setStyle(optCardStyle(true));
                }
            });

            vbox.getChildren().add(card);
        }
        optionsContainer.getChildren().add(vbox);
    }

    // =========================================================================
    // SAVE ANSWER
    // =========================================================================

    private void saveCurrentAnswer() {
        if (currentIndex < 0 || currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        if (q.isMCQ()) {
            if (mcqToggleGroup != null) {
                Toggle sel = mcqToggleGroup.getSelectedToggle();
                if (sel != null) answers.put(q.getId(), sel.getUserData().toString());
            }
        } else {
            String txt = subjectiveArea.getText();
            if (txt != null && !txt.isBlank()) answers.put(q.getId(), txt.trim());
        }
    }

    // =========================================================================
    // TIMER
    // =========================================================================

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            Platform.runLater(() -> {
                timerLabel.setText(formatTime(remainingSeconds));
                if (remainingSeconds <= 60) {
                    timerLabel.setTextFill(Color.web("#ef5350"));
                    timerLabel.setStyle("-fx-effect: dropshadow(gaussian,rgba(239,83,80,0.6),8,0,0,0);");
                } else if (remainingSeconds <= 300) {
                    timerLabel.setTextFill(Color.web("#ff9800"));
                }
                if (remainingSeconds <= 0) { timer.stop(); autoSubmit(); }
            });
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private String formatTime(int sec) {
        int h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    private void autoSubmit() {
        if (examSubmitted) return;
        Platform.runLater(() -> {
            saveCurrentAnswer();
            AlertHelper.showWarning("Time's Up!", "Your time has expired. The exam will be submitted automatically.");
            submitExam(true);
        });
    }

    // =========================================================================
    // ANTI-CHEAT
    // =========================================================================

    private void setupFocusDetection() {
        Stage stage = SceneManager.getInstance().getPrimaryStage();
        stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && !examSubmitted) {
                focusLossCount++;
                Platform.runLater(() -> {
                    if (focusLossCount >= MAX_FOCUS_LOSS) {
                        if (timer != null) timer.stop();
                        AlertHelper.showWarning("Exam Terminated",
                                "You switched windows " + MAX_FOCUS_LOSS + " times.\n" +
                                        "Your exam has been auto-submitted for security reasons.");
                        submitExam(true);
                    } else {
                        int rem = MAX_FOCUS_LOSS - focusLossCount;
                        AlertHelper.showWarning("Warning — Focus Lost",
                                "Do NOT switch away from the exam!\n" +
                                        "Warning " + focusLossCount + " of " + MAX_FOCUS_LOSS + ".\n" +
                                        rem + " more violation(s) will auto-submit your exam.");
                    }
                });
            }
        });
    }

    // =========================================================================
    // SUBMIT
    // =========================================================================

    private void confirmAndSubmit(boolean auto) {
        if (examSubmitted) return;
        long answered   = answers.values().stream().filter(v -> !v.isBlank()).count();
        long unanswered = questions.size() - answered;
        String msg = "Submit exam now?\n\nAnswered : " + answered + " / " + questions.size() + "\n"
                + (unanswered > 0
                ? "Unanswered: " + unanswered + " question(s)\n\nUnanswered questions receive 0 marks."
                : "All questions answered!");
        if (!auto && !AlertHelper.showConfirm("Submit Exam", msg)) return;
        submitExam(auto);
    }

    private void submitExam(boolean autoSubmitted) {
        if (examSubmitted) return;
        examSubmitted = true;
        if (timer != null) timer.stop();
        saveCurrentAnswer();

        long timeTaken = (System.currentTimeMillis() - startTimestamp) / 1000L;
        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        String fullName = SessionManager.getInstance().getCurrentUser().getFullName();

        ExamResult result = new ExamResult(exam.getId(), exam.getTitle(), username, fullName);
        result.setTotalMarks(exam.getTotalMarks());
        result.setAnswers(new HashMap<>(answers));
        result.setSubmittedAt(LocalDateTime.now());
        result.setTimeTakenSeconds(timeTaken);
        result.setAutoSubmitted(autoSubmitted);
        result.calculateScore(questions);

        ResultService.getInstance().saveResult(result);

        com.examapp.model.User user = SessionManager.getInstance().getCurrentUser();
        if (user instanceof com.examapp.model.Student) {
            com.examapp.model.Student s = (com.examapp.model.Student) user;
            s.recordAttempt(exam.getId());
            com.examapp.util.FileManager.saveUser(s);
        }

        Platform.runLater(() -> SceneManager.getInstance().showResult(result, exam));
    }

    // =========================================================================
    // STYLE HELPERS
    // =========================================================================

    private String dotStyle(boolean active, boolean answered) {
        if (active)   return "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand;";
        if (answered) return "-fx-background-color: #43a047; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand;";
        return "-fx-background-color: #e0e0e0; -fx-text-fill: #455a64; -fx-background-radius: 16; -fx-cursor: hand;";
    }

    private String optCardStyle(boolean selected) {
        return selected
                ? "-fx-background-color: #e3f2fd; -fx-background-radius: 10; -fx-border-color: #1565c0; -fx-border-width: 2; -fx-border-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: #f5f7fb; -fx-background-radius: 10; -fx-border-color: #e0e6f0; -fx-border-width: 1.5; -fx-border-radius: 10; -fx-cursor: hand;";
    }

    private String navBtnStyle(boolean primary) {
        return primary
                ? "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;";
    }

    public BorderPane getView() { return root; }
}