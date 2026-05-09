package com.examapp.controller;

import com.examapp.model.Exam;
import com.examapp.model.Question;
import com.examapp.model.QuestionType;
import com.examapp.service.ExamService;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Admin screen to add, view, and remove questions for a specific exam.
 */
public class ManageQuestionsController {

    private BorderPane root;
    private Exam exam;
    private VBox questionListBox;

    // Add question form fields
    private ToggleGroup typeGroup;
    private RadioButton mcqRadio;
    private RadioButton subjectiveRadio;
    private TextArea questionTextArea;
    private TextField optAField, optBField, optCField, optDField;
    private ToggleGroup answerGroup;
    private RadioButton ansA, ansB, ansC, ansD;
    private TextField marksField;
    private VBox mcqOptionsPane;
    private Label formError;

    public ManageQuestionsController(Exam exam) {
        this.exam = exam;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4ff;");

        // Top bar
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 24, 0, 24));
        topBar.setPrefHeight(60);
        topBar.setStyle("-fx-background-color: #1565c0; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");

        Button backBtn = new Button("Back");
        backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> SceneManager.getInstance().showAdminDashboard());

        Label pageTitle = new Label("Manage Questions  |  " + exam.getTitle());
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pageTitle.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label();
        countLabel.setFont(Font.font("Arial", 13));
        countLabel.setTextFill(Color.web("#bbdefb"));
        updateCountLabel(countLabel);

        topBar.getChildren().addAll(backBtn, pageTitle, spacer, countLabel);
        root.setTop(topBar);

        // Split: left = add question form, right = question list
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.42);
        splitPane.setOrientation(Orientation.HORIZONTAL);

        ScrollPane leftScroll = new ScrollPane(buildAddQuestionForm());
        leftScroll.setFitToWidth(true);
        leftScroll.setStyle("-fx-background: #f0f4ff; -fx-background-color: #f0f4ff;");

        ScrollPane rightScroll = buildQuestionList();

        splitPane.getItems().addAll(leftScroll, rightScroll);
        root.setCenter(splitPane);
    }

    private void updateCountLabel(Label label) {
        // Reload fresh exam
        ExamService.getInstance().getExamById(exam.getId()).ifPresent(e -> exam = e);
        int total = exam.getQuestions().size();
        label.setText(total + " Questions  |  " + exam.getTotalMarks() + " Total Marks");
    }

    private VBox buildAddQuestionForm() {
        VBox form = new VBox(16);
        form.setPadding(new Insets(24));

        Label heading = new Label("Add Question");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        heading.setTextFill(Color.web("#1a237e"));

        // Question type radio
        typeGroup = new ToggleGroup();
        mcqRadio = new RadioButton("Multiple Choice (MCQ)");
        subjectiveRadio = new RadioButton("Subjective");
        mcqRadio.setToggleGroup(typeGroup);
        subjectiveRadio.setToggleGroup(typeGroup);
        mcqRadio.setSelected(true);
        mcqRadio.setStyle("-fx-font-size: 13;");
        subjectiveRadio.setStyle("-fx-font-size: 13;");

        HBox typeBox = new HBox(20, mcqRadio, subjectiveRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        typeBox.setPadding(new Insets(8, 12, 8, 12));
        typeBox.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 8;");

        // Question text
        questionTextArea = new TextArea();
        questionTextArea.setPromptText("Enter your question here...");
        questionTextArea.setPrefRowCount(4);
        questionTextArea.setWrapText(true);
        styleTextArea(questionTextArea);

        // MCQ options pane
        mcqOptionsPane = buildMCQOptionsPane();

        // Marks field
        marksField = new TextField("1");
        marksField.setPromptText("e.g. 1");
        styleField(marksField);
        marksField.setPrefWidth(80);

        formError = new Label("");
        formError.setTextFill(Color.web("#c62828"));
        formError.setFont(Font.font("Arial", 13));
        formError.setWrapText(true);

        Button addBtn = new Button("Add Question");
        addBtn.setPrefWidth(Double.MAX_VALUE);
        addBtn.setPrefHeight(44);
        addBtn.setStyle(
                "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14;" +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(addBtn.getStyle().replace("#2e7d32", "#1b5e20")));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(addBtn.getStyle().replace("#1b5e20", "#2e7d32")));
        addBtn.setOnAction(e -> handleAddQuestion());

        // Toggle MCQ fields visibility
        typeGroup.selectedToggleProperty().addListener((obs, old, newT) -> {
            boolean isMCQ = newT == mcqRadio;
            mcqOptionsPane.setVisible(isMCQ);
            mcqOptionsPane.setManaged(isMCQ);
        });

        form.getChildren().addAll(
                heading,
                fieldBlock("Question Type", typeBox),
                fieldBlock("Question Text *", questionTextArea),
                mcqOptionsPane,
                fieldBlock("Marks *", marksField),
                formError,
                addBtn
        );

        return form;
    }

    private VBox buildMCQOptionsPane() {
        VBox pane = new VBox(12);

        optAField = new TextField(); optAField.setPromptText("Option A"); styleField(optAField);
        optBField = new TextField(); optBField.setPromptText("Option B"); styleField(optBField);
        optCField = new TextField(); optCField.setPromptText("Option C"); styleField(optCField);
        optDField = new TextField(); optDField.setPromptText("Option D"); styleField(optDField);

        answerGroup = new ToggleGroup();
        ansA = new RadioButton("A"); ansA.setToggleGroup(answerGroup); ansA.setSelected(true);
        ansB = new RadioButton("B"); ansB.setToggleGroup(answerGroup);
        ansC = new RadioButton("C"); ansC.setToggleGroup(answerGroup);
        ansD = new RadioButton("D"); ansD.setToggleGroup(answerGroup);

        HBox answerRow = new HBox(20, new Label("Correct Answer: "), ansA, ansB, ansC, ansD);
        answerRow.setAlignment(Pos.CENTER_LEFT);
        answerRow.setPadding(new Insets(8, 12, 8, 12));
        answerRow.setStyle("-fx-background-color: #fff8e1; -fx-background-radius: 8;");

        Label optLabel = new Label("Answer Options *");
        optLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        optLabel.setTextFill(Color.web("#455a64"));

        pane.getChildren().addAll(
                optLabel,
                labeledField("A", optAField),
                labeledField("B", optBField),
                labeledField("C", optCField),
                labeledField("D", optDField),
                answerRow
        );

        return pane;
    }

    private HBox labeledField(String label, TextField field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#1565c0"));
        lbl.setMinWidth(16);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(lbl, field);
        return row;
    }

    private ScrollPane buildQuestionList() {
        questionListBox = new VBox(12);
        questionListBox.setPadding(new Insets(24));

        refreshQuestionList();

        ScrollPane scroll = new ScrollPane(questionListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f8f9fe; -fx-background-color: #f8f9fe;");
        return scroll;
    }

    private void refreshQuestionList() {
        questionListBox.getChildren().clear();

        ExamService.getInstance().getExamById(exam.getId()).ifPresent(e -> exam = e);

        Label heading = new Label("Questions in This Exam");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        heading.setTextFill(Color.web("#1a237e"));
        heading.setPadding(new Insets(0, 0, 8, 0));
        questionListBox.getChildren().add(heading);

        List<Question> questions = exam.getQuestions();

        if (questions.isEmpty()) {
            Label empty = new Label("No questions yet. Add some using the form on the left.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.web("#90a4ae"));
            questionListBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            questionListBox.getChildren().add(buildQuestionCard(q, i + 1));
        }
    }

    private VBox buildQuestionCard(Question q, int index) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 1);"
        );

        // Header row: question number + type badge + delete button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label("Q" + index);
        numLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        numLabel.setTextFill(Color.web("#1565c0"));
        numLabel.setMinWidth(30);

        String typeColor = q.isMCQ() ? "#0277bd" : "#6a1b9a";
        String typeBg    = q.isMCQ() ? "#e1f5fe" : "#f3e5f5";
        Label typeBadge = new Label(q.isMCQ() ? "MCQ" : "Subjective");
        typeBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        typeBadge.setTextFill(Color.web(typeColor));
        typeBadge.setPadding(new Insets(3, 10, 3, 10));
        typeBadge.setStyle("-fx-background-color: " + typeBg + "; -fx-background-radius: 20;");

        Label marksLbl = new Label(q.getMarks() + " mark" + (q.getMarks() != 1 ? "s" : ""));
        marksLbl.setFont(Font.font("Arial", 12));
        marksLbl.setTextFill(Color.web("#78909c"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Remove");
        deleteBtn.setStyle(
                "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-size: 12;" +
                        "-fx-background-radius: 7; -fx-padding: 4 12; -fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            if (AlertHelper.showConfirm("Remove Question", "Remove this question from the exam?")) {
                ExamService.getInstance().removeQuestionFromExam(exam.getId(), q.getId());
                refreshQuestionList();
            }
        });

        header.getChildren().addAll(numLabel, typeBadge, marksLbl, spacer, deleteBtn);

        // Question text
        Label questionText = new Label(q.getQuestionText());
        questionText.setFont(Font.font("Arial", 13));
        questionText.setTextFill(Color.web("#263238"));
        questionText.setWrapText(true);

        card.getChildren().addAll(header, questionText);

        // MCQ options
        if (q.isMCQ()) {
            VBox optionsBox = new VBox(4);
            optionsBox.setPadding(new Insets(4, 0, 0, 0));
            String[] labels = {"A", "B", "C", "D"};
            List<String> opts = q.getOptions();
            for (int i = 0; i < opts.size(); i++) {
                String letter = labels[i];
                boolean isCorrect = letter.equals(q.getCorrectAnswer());
                Label optLabel = new Label((isCorrect ? "  " : "  ") + letter + ".  " + opts.get(i));
                optLabel.setFont(Font.font("Arial", isCorrect ? FontWeight.BOLD : FontWeight.NORMAL, 12));
                optLabel.setTextFill(isCorrect ? Color.web("#2e7d32") : Color.web("#546e7a"));
                if (isCorrect) {
                    optLabel.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 5; -fx-padding: 2 8;");
                }
                optionsBox.getChildren().add(optLabel);
            }
            card.getChildren().add(optionsBox);
        }

        return card;
    }

    private void handleAddQuestion() {
        formError.setText("");

        String questionText = questionTextArea.getText().trim();
        if (questionText.isEmpty()) {
            formError.setText("Question text is required.");
            return;
        }

        int marks;
        try {
            marks = Integer.parseInt(marksField.getText().trim());
            if (marks < 1 || marks > 20) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            formError.setText("Marks must be a number between 1 and 20.");
            return;
        }

        boolean isMCQ = mcqRadio.isSelected();
        Question question;
        int currentCount = exam.getQuestions().size();

        if (isMCQ) {
            String optA = optAField.getText().trim();
            String optB = optBField.getText().trim();
            String optC = optCField.getText().trim();
            String optD = optDField.getText().trim();

            if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                formError.setText("All four MCQ options are required.");
                return;
            }

            String correct = "A";
            if (ansB.isSelected()) correct = "B";
            else if (ansC.isSelected()) correct = "C";
            else if (ansD.isSelected()) correct = "D";

            question = Question.createMCQ(exam.getId(), questionText,
                    optA, optB, optC, optD, correct, marks, currentCount);
        } else {
            question = Question.createSubjective(exam.getId(), questionText, marks, currentCount);
        }

        ExamService.getInstance().addQuestionToExam(exam.getId(), question);

        // Reload exam reference
        ExamService.getInstance().getExamById(exam.getId()).ifPresent(e -> exam = e);

        // Clear form
        questionTextArea.clear();
        optAField.clear(); optBField.clear(); optCField.clear(); optDField.clear();
        ansA.setSelected(true);
        marksField.setText("1");
        mcqRadio.setSelected(true);
        mcqOptionsPane.setVisible(true);
        mcqOptionsPane.setManaged(true);
        formError.setText("");

        refreshQuestionList();
        AnimationUtil.pulse(questionListBox);
    }

    private void styleField(TextField field) {
        field.setStyle(
                "-fx-background-color: #f5f7fb; -fx-border-color: #cfd8e3;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7;" +
                        "-fx-padding: 8 12; -fx-font-size: 13; -fx-pref-height: 38;"
        );
    }

    private void styleTextArea(TextArea area) {
        area.setStyle(
                "-fx-background-color: #f5f7fb; -fx-border-color: #cfd8e3;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 10; -fx-font-size: 13;"
        );
    }

    private VBox fieldBlock(String labelText, javafx.scene.Node field) {
        VBox block = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#455a64"));
        block.getChildren().addAll(lbl, field);
        return block;
    }

    public BorderPane getView() {
        return root;
    }
}