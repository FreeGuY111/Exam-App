package com.examapp.ui;

import com.examapp.service.SessionManager;

/**
 * Manages application-wide theme toggling.
 */
public class ThemeManager {

    private ThemeManager() {}

    public static void toggleTheme() {
        SessionManager.getInstance().toggleDarkMode();
        SceneManager.getInstance().refreshTheme();
    }

    public static boolean isDarkMode() {
        return SessionManager.getInstance().isDarkMode();
    }

    public static String getThemeLabel() {
        return isDarkMode() ? "Switch to Light Mode" : "Switch to Dark Mode";
    }
}