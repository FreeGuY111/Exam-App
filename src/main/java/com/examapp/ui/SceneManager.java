package com.examapp.ui;

import com.examapp.controller.*;
import com.examapp.model.Exam;
import com.examapp.model.ExamResult;
import com.examapp.service.SessionManager;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Centralized scene/screen navigation manager.
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void switchScene(Pane root, String title) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            applyTheme(scene);
            primaryStage.setScene(scene);
        } else {
            applyTheme(scene);
            scene.setRoot(root);
        }
        primaryStage.setTitle(title + " | ExamPro");

        // Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.millis(250), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String css = getClass().getResource("/css/main.css") != null
                ? getClass().getResource("/css/main.css").toExternalForm() : null;
        if (css != null) scene.getStylesheets().add(css);

        if (SessionManager.getInstance().isDarkMode()) {
            String darkCss = getClass().getResource("/css/dark.css") != null
                    ? getClass().getResource("/css/dark.css").toExternalForm() : null;
            if (darkCss != null) scene.getStylesheets().add(darkCss);
        }
    }

    public void refreshTheme() {
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    public void showSplash() {
        SplashController controller = new SplashController();
        switchScene(controller.getView(), "Welcome");
    }

    public void showLogin() {
        LoginController controller = new LoginController();
        switchScene(controller.getView(), "Login");
    }

    public void showAdminDashboard() {
        AdminDashController controller = new AdminDashController();
        switchScene(controller.getView(), "Admin Dashboard");
    }

    public void showStudentDashboard() {
        StudentDashController controller = new StudentDashController();
        switchScene(controller.getView(), "Student Dashboard");
    }

    public void showCreateExam() {
        CreateExamController controller = new CreateExamController();
        switchScene(controller.getView(), "Create Exam");
    }

    public void showManageQuestions(Exam exam) {
        ManageQuestionsController controller = new ManageQuestionsController(exam);
        switchScene(controller.getView(), "Manage Questions - " + exam.getTitle());
    }

    public void showViewResults() {
        ViewResultsController controller = new ViewResultsController();
        switchScene(controller.getView(), "View Results");
    }

    public void showExam(Exam exam) {
        ExamController controller = new ExamController(exam);
        switchScene(controller.getView(), "Exam - " + exam.getTitle());
    }

    public void showResult(ExamResult result, Exam exam) {
        ResultController controller = new ResultController(result, exam);
        switchScene(controller.getView(), "Result - " + exam.getTitle());
    }

    public void showChangePassword() {
        ChangePasswordController controller = new ChangePasswordController();
        switchScene(controller.getView(), "Change Password");
    }
}