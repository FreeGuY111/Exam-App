package com.examapp.controller;

import com.examapp.model.ExamResult;
import com.examapp.service.ResultService;
import com.examapp.ui.AlertHelper;
import com.examapp.ui.AnimationUtil;
import com.examapp.ui.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin screen showing all student exam results in a searchable table.
 */
public class ViewResultsController {

    private BorderPane root;
    private TableView<ExamResult> table;
    private ObservableList<ExamResult> allResults;
    private TextField searchField;

    public ViewResultsController() {
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

        Label pageTitle = new Label("Student Results");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pageTitle.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(backBtn, pageTitle);
        root.setTop(topBar);

        // Content
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        // Stats row
        List<ExamResult> results = ResultService.getInstance().getAllResults();
        allResults = FXCollections.observableArrayList(results);

        long passed = results.stream().filter(ExamResult::isPassed).count();
        long failed = results.size() - passed;
        double avg = results.stream().mapToDouble(ExamResult::getPercentage).average().orElse(0);

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                statCard("Total Attempts", String.valueOf(results.size()), "#1565c0"),
                statCard("Passed", String.valueOf(passed), "#2e7d32"),
                statCard("Failed", String.valueOf(failed), "#c62828"),
                statCard("Avg Score", String.format("%.1f%%", avg), "#e65100")
        );

        // Search bar
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by student name, username, or exam title...");
        searchField.setPrefWidth(400);
        searchField.setPrefHeight(38);
        searchField.setStyle(
                "-fx-background-color: white; -fx-border-color: #cfd8e3; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14; -fx-font-size: 13;"
        );
        searchField.textProperty().addListener((obs, old, val) -> filterResults(val));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;"
        );
        refreshBtn.setOnAction(e -> {
            List<ExamResult> fresh = ResultService.getInstance().getAllResults();
            allResults.setAll(fresh);
            searchField.clear();
        });

        Button exportAllBtn = new Button("Export Selected");
        exportAllBtn.setStyle(
                "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;"
        );
        exportAllBtn.setOnAction(e -> {
            ExamResult selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertHelper.showWarning("No Selection", "Please select a result row to export.");
                return;
            }
            String path = ResultService.getInstance().exportResult(selected);
            if (path != null) {
                AlertHelper.showInfo("Exported", "Result exported to:\n" + path);
            } else {
                AlertHelper.showError("Export Failed", "Could not write export file.");
            }
        });

        searchBar.getChildren().addAll(searchField, refreshBtn, exportAllBtn);

        // Table
        table = buildTable();

        content.getChildren().addAll(statsRow, searchBar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(content);

        AnimationUtil.fadeIn(content, 250);
    }

    private TableView<ExamResult> buildTable() {
        TableView<ExamResult> tv = new TableView<>(allResults);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        tv.setPlaceholder(new Label("No results found."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

        TableColumn<ExamResult, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentFullName()));
        colStudent.setMinWidth(140);

        TableColumn<ExamResult, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentUsername()));
        colUsername.setMinWidth(110);

        TableColumn<ExamResult, String> colExam = new TableColumn<>("Exam");
        colExam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExamTitle()));
        colExam.setMinWidth(180);

        TableColumn<ExamResult, String> colScore = new TableColumn<>("Score");
        colScore.setCellValueFactory(c -> {
            ExamResult r = c.getValue();
            return new SimpleStringProperty(r.getFinalScore() + " / " + r.getTotalMarks());
        });
        colScore.setMinWidth(90);

        TableColumn<ExamResult, String> colPct = new TableColumn<>("Percentage");
        colPct.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%.1f%%", c.getValue().getPercentage())));
        colPct.setMinWidth(100);

        TableColumn<ExamResult, String> colResult = new TableColumn<>("Result");
        colResult.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isPassed() ? "PASS" : "FAIL"));
        colResult.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    if ("PASS".equals(item)) {
                        setStyle("-fx-text-fill: #2e7d32;");
                    } else {
                        setStyle("-fx-text-fill: #c62828;");
                    }
                }
            }
        });
        colResult.setMinWidth(70);

        TableColumn<ExamResult, String> colDate = new TableColumn<>("Submitted");
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSubmittedAt() != null
                        ? c.getValue().getSubmittedAt().format(fmt) : "—"));
        colDate.setMinWidth(160);

        TableColumn<ExamResult, String> colTime = new TableColumn<>("Time Taken");
        colTime.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormattedTime()));
        colTime.setMinWidth(100);

        TableColumn<ExamResult, String> colAuto = new TableColumn<>("Auto-Submit");
        colAuto.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isAutoSubmitted() ? "Yes" : "No"));
        colAuto.setMinWidth(90);

        tv.getColumns().addAll(colStudent, colUsername, colExam, colScore, colPct, colResult, colDate, colTime, colAuto);

        // Style alternate rows
        tv.setRowFactory(tableView -> {
            TableRow<ExamResult> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, result) -> {
                if (result == null) return;
                if (!result.isPassed()) {
                    row.setStyle("-fx-background-color: #fff8f8;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });

        return tv;
    }

    private void filterResults(String query) {
        if (query == null || query.isBlank()) {
            table.setItems(allResults);
            return;
        }
        String lq = query.toLowerCase().trim();
        List<ExamResult> filtered = allResults.stream()
                .filter(r ->
                        (r.getStudentFullName() != null && r.getStudentFullName().toLowerCase().contains(lq)) ||
                                (r.getStudentUsername() != null && r.getStudentUsername().toLowerCase().contains(lq)) ||
                                (r.getExamTitle() != null && r.getExamTitle().toLowerCase().contains(lq))
                )
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setPrefHeight(80);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 2);"
        );
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        val.setTextFill(Color.web(color));
        Label name = new Label(label);
        name.setFont(Font.font("Arial", 12));
        name.setTextFill(Color.web("#78909c"));
        card.getChildren().addAll(val, name);
        return card;
    }

    public BorderPane getView() {
        return root;
    }
}