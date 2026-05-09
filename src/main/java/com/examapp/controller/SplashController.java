package com.examapp.controller;

import com.examapp.service.AuthService;
import com.examapp.service.ExamService;
import com.examapp.ui.SceneManager;
import com.examapp.util.Constants;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Splash screen controller with animated loading sequence.
 */
public class SplashController {

    private StackPane root;

    public SplashController() {
        buildUI();
    }

    private void buildUI() {
        root = new StackPane();
        root.setPrefSize(Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0d47a1, #1565c0, #1976d2, #42a5f5);");

        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(480);
        centerBox.setOpacity(0);

        // Logo label
        Label logoIcon = new Label("E");
        logoIcon.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        logoIcon.setTextFill(Color.WHITE);
        logoIcon.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 16 28 16 28;"
        );

        // App name
        Label appName = new Label("ExamPro");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        appName.setTextFill(Color.WHITE);

        // Tagline
        Label tagline = new Label("Online Examination System");
        tagline.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        tagline.setTextFill(Color.web("#bbdefb"));

        // Version
        Label version = new Label("Version " + Constants.APP_VERSION);
        version.setFont(Font.font("Arial", 13));
        version.setTextFill(Color.web("#90caf9"));

        // Progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(360);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
                "-fx-accent: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
        );

        // Loading label
        Label loadingLabel = new Label("Initializing...");
        loadingLabel.setFont(Font.font("Arial", 14));
        loadingLabel.setTextFill(Color.web("#e3f2fd"));

        centerBox.getChildren().addAll(
                logoIcon, appName, tagline, version, progressBar, loadingLabel
        );
        root.getChildren().add(centerBox);

        // Fade in the whole card
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), centerBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Scale logo icon
        ScaleTransition logoScale = new ScaleTransition(Duration.millis(600), logoIcon);
        logoScale.setFromX(0.3);
        logoScale.setFromY(0.3);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);
        logoScale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fadeIn, logoScale).play();

        // Progress animation steps
        String[] messages  = {"Initializing...", "Loading services...", "Preparing data...", "Almost ready..."};
        double[] progress  = {0.15, 0.45, 0.75, 1.0};

        Timeline progressTimeline = new Timeline();

        for (int i = 0; i < messages.length; i++) {
            final int idx = i;
            long delayMs = 400L + idx * 550L;

            progressTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(delayMs), e -> {
                        loadingLabel.setText(messages[idx]);
                        Timeline barAnim = new Timeline(
                                new KeyFrame(
                                        Duration.millis(380),
                                        new KeyValue(progressBar.progressProperty(), progress[idx])
                                )
                        );
                        barAnim.play();
                    })
            );
        }

        progressTimeline.setOnFinished(e -> {
            // Initialise services (creates default admin + sample exam)
            AuthService.getInstance();
            ExamService.getInstance();

            PauseTransition pause = new PauseTransition(Duration.millis(400));
            pause.setOnFinished(ev ->
                    Platform.runLater(() -> SceneManager.getInstance().showLogin())
            );
            pause.play();
        });

        progressTimeline.play();
    }

    public StackPane getView() {
        return root;
    }
}