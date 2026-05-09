package com.examapp.ui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class for reusable JavaFX animations.
 */
public class AnimationUtil {

    private AnimationUtil() {}

    public static void fadeIn(Node node, int durationMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public static void slideInFromLeft(Node node, int durationMs) {
        node.setTranslateX(-80);
        node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setToValue(1);
        new ParallelTransition(tt, ft).play();
    }

    public static void slideInFromRight(Node node, int durationMs) {
        node.setTranslateX(80);
        node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setToValue(1);
        new ParallelTransition(tt, ft).play();
    }

    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.06);
        st.setToY(1.06);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    public static void shakeNode(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    public static Timeline buildProgressBarAnimation(javafx.scene.control.ProgressBar bar,
                                                     double targetProgress, int durationMs) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(durationMs),
                        new KeyValue(bar.progressProperty(), targetProgress))
        );
        return timeline;
    }
}