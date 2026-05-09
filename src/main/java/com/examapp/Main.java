package com.examapp;

import com.examapp.service.SessionManager;
import com.examapp.ui.SceneManager;
import com.examapp.util.Constants;
import com.examapp.util.FileManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * Main JavaFX Application entry point for the Online Examination System.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize file manager (creates data dirs if missing)
        FileManager.initialize();

        // Initialize session manager
        SessionManager.getInstance();

        // Set up scene manager with the primary stage
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);

        // Configure the primary stage
        primaryStage.setTitle(Constants.APP_NAME);
        primaryStage.setMinWidth(Constants.MIN_WIDTH);
        primaryStage.setMinHeight(Constants.MIN_HEIGHT);
        primaryStage.setWidth(Constants.DEFAULT_WIDTH);
        primaryStage.setHeight(Constants.DEFAULT_HEIGHT);

        // Set app icon if available
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
            // Icon not found, continue without it
        }

        // Handle close request properly
        primaryStage.setOnCloseRequest(event -> {
            SessionManager.getInstance().logout();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        // Start with Splash Screen
        sceneManager.showSplash();
    }

    public static void main(String[] args) {
        launch(args);
    }
}