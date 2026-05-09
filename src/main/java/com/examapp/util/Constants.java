package com.examapp.util;

/**
 * Application-wide constants.
 */
public final class Constants {

    private Constants() {}

    // Application info
    public static final String APP_NAME = "ExamPro - Online Examination System";
    public static final String APP_VERSION = "1.0.0";

    // Window dimensions
    public static final double DEFAULT_WIDTH = 1100.0;
    public static final double DEFAULT_HEIGHT = 720.0;
    public static final double MIN_WIDTH = 900.0;
    public static final double MIN_HEIGHT = 600.0;

    // Data directory (relative to user home)
    public static final String DATA_DIR = System.getProperty("user.home") + "/ExamProData";
    public static final String USERS_FILE = DATA_DIR + "/users.dat";
    public static final String EXAMS_FILE = DATA_DIR + "/exams.dat";
    public static final String RESULTS_FILE = DATA_DIR + "/results.dat";
    public static final String EXPORT_DIR = DATA_DIR + "/exports";

    // Default admin credentials
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    // Exam engine
    public static final int DEFAULT_EXAM_DURATION_MINUTES = 30;
    public static final int MIN_QUESTIONS_PER_EXAM = 5;
    public static final int MAX_ATTEMPTS_PER_STUDENT = 1;

    // Animation durations (ms)
    public static final int SPLASH_DURATION_MS = 2800;
    public static final int TRANSITION_DURATION_MS = 300;

    // Theme
    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";

    // Question types
    public static final String TYPE_MCQ = "MCQ";
    public static final String TYPE_SUBJECTIVE = "SUBJECTIVE";

    // Pass percentage
    public static final double PASS_PERCENTAGE = 50.0;
}