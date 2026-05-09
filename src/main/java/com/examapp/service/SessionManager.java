package com.examapp.service;

import com.examapp.model.User;

/**
 * Singleton session manager that tracks the currently logged-in user.
 */
public class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    private long loginTimestamp;
    private boolean darkMode = false;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTimestamp = System.currentTimeMillis();
    }

    public void logout() {
        this.currentUser = null;
        this.loginTimestamp = 0;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isStudent() {
        return currentUser != null && currentUser.isStudent();
    }

    public long getLoginTimestamp() {
        return loginTimestamp;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void toggleDarkMode() {
        this.darkMode = !this.darkMode;
    }
}