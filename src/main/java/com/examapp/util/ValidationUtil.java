package com.examapp.util;

/**
 * Input validation utility methods.
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidUsername(String username) {
        if (isNullOrBlank(username)) return false;
        return username.matches("[a-zA-Z0-9_]{3,20}");
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) return false;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isPositiveInt(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"']", "");
    }
}