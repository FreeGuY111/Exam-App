package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.service.ExamService;
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
 * Admin screen for creating a new exam.
 */
public class CreateExamController {

    private BorderPane root;

    public CreateExamController() {
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
        topBar.setStyle("-fx-background-color: #1565c0;");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;");
        backBtn.setOnAction(e -> SceneManager.getInstance().showAdminDashboard());

        Label pageTitle = new Label("  Create New Exam");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pageTitle.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(backBtn, pageTitle);
        root.setTop(topBar);

        // Center: form card
        ScrollPane scroll = new ScrollPane(buildFormCard());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f0f4ff; -fx-background-color: #f0f4ff;");
        root.setCenter(scroll);
    }

    private VBox buildFormCard() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setPadding(new Insets(40));

        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setPadding(new Insets(36));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0, 0, 3);"
        );

        Label heading = new Label("Exam Details");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        heading.setTextFill(Color.web("#1a237e"));

        // Fields
        TextField titleField = styledField("e.g. Java Programming Fundamentals");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Brief description of this exam...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        styleTextArea(descArea);

        TextField durationField = styledField("e.g. 30");
        CheckBox randomizeCheck = new CheckBox("Randomize question order");
        randomizeCheck.setFont(Font.font("Arial", 13));

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#c62828"));
        errorLabel.setFont(Font.font("Arial", 13));
        errorLabel.setWrapText(true);

        Button createBtn = new Button("Create Exam & Add Questions");
        createBtn.setPrefWidth(Double.MAX_VALUE);
        createBtn.setStyle(
                "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 13 0; -fx-cursor: hand;"
        );
        createBtn.setOnMouseEntered(e -> createBtn.setStyle(createBtn.getStyle().replace("#1565c0", "#0d47a1")));
        createBtn.setOnMouseExited(e -> createBtn.setStyle(createBtn.getStyle().replace("#0d47a1", "#1565c0")));

        createBtn.setOnAction(e -> {
            errorLabel.setText("");
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            String durStr = durationField.getText().trim();

            if (title.isEmpty()) { errorLabel.setText("Exam title is required."); return; }
            if (title.length() < 4) { errorLabel.setText("Title must be at least 4 characters."); return; }
            if (durStr.isEmpty()) { errorLabel.setText("Duration is required."); return; }

            int duration;
            try {
                duration = Integer.parseInt(durStr);
                if (duration < 1 || duration > 300) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Duration must be a number between 1 and 300 minutes.");
                return;
            }

            Exam exam = new Exam(title, desc,
                    SessionManager.getInstance().getCurrentUser().getUsername(), duration);
            exam.setRandomizeQuestions(randomizeCheck.isSelected());
            ExamService.getInstance().saveExam(exam);

            AlertHelper.showInfo("Exam Created",
                    "Exam '" + title + "' created successfully!\nNow add questions to it.");
            SceneManager.getInstance().showManageQuestions(exam);
        });

        card.getChildren().addAll(
                heading,
                fieldBlock("Exam Title *", titleField),
                fieldBlock("Description (Optional)", descArea),
                fieldBlock("Duration (minutes) *", durationField),
                randomizeCheck,
                errorLabel,
                createBtn
        );

        outer.getChildren().add(card);
        AnimationUtil.fadeIn(card, 300);
        return outer;
    }

    private VBox fieldBlock(String label, Control field) {
        VBox block = new VBox(6);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#37474f"));
        block.getChildren().addAll(lbl, field);
        return block;
    }

    private TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-background-color: #f5f7fb; -fx-border-color: #cfd8e3; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14; -fx-pref-height: 42;"
        );
        return field;
    }

    private void styleTextArea(TextArea area) {
        area.setStyle(
                "-fx-background-color: #f5f7fb; -fx-border-color: #cfd8e3; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 13;"
        );
    }

    public BorderPane getView() {
        return root;
    }
}