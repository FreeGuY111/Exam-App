package com.examapp.service;

import com.examapp.model.Admin;
import com.examapp.model.Student;
import com.examapp.model.User;
import com.examapp.util.Constants;
import com.examapp.util.FileManager;
import com.examapp.util.PasswordUtil;
import com.examapp.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service handling login, registration, and password management.
 */
public class AuthService {

    private static AuthService instance;

    private AuthService() {
        ensureDefaultAdminExists();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Creates the default admin account if no admin exists yet.
     */
    private void ensureDefaultAdminExists() {
        List<User> users = FileManager.loadUsers();
        boolean adminExists = users.stream().anyMatch(User::isAdmin);
        if (!adminExists) {
            Admin defaultAdmin = new Admin(
                    Constants.DEFAULT_ADMIN_USERNAME,
                    PasswordUtil.hashPassword(Constants.DEFAULT_ADMIN_PASSWORD),
                    "System Administrator",
                    "admin@examapp.com"
            );
            FileManager.saveUser(defaultAdmin);
        }
    }

    /**
     * Authenticate a user with the given credentials.
     *
     * @return the logged-in User, or null if authentication fails
     */
    public User login(String username, String password, String role) {
        if (ValidationUtil.isNullOrBlank(username) || ValidationUtil.isNullOrBlank(password)) {
            return null;
        }

        Optional<User> userOpt = FileManager.findUserByUsername(username.trim());
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();

        // Check role match
        if (!user.getRole().equals(role)) return null;

        // Check account active
        if (!user.isActive()) return null;

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) return null;

        return user;
    }

    /**
     * Register a new student account.
     *
     * @return null on success, or an error message string
     */
    public String registerStudent(String username, String password, String confirmPassword,
                                  String fullName, String email) {
        if (ValidationUtil.isNullOrBlank(username)) return "Username is required.";
        if (!ValidationUtil.isValidUsername(username)) return "Username must be 3-20 alphanumeric characters.";
        if (ValidationUtil.isNullOrBlank(fullName)) return "Full name is required.";
        if (ValidationUtil.isNullOrBlank(email)) return "Email is required.";
        if (!ValidationUtil.isValidEmail(email)) return "Enter a valid email address.";
        if (ValidationUtil.isNullOrBlank(password)) return "Password is required.";
        if (!ValidationUtil.isValidPassword(password)) return "Password must be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "Passwords do not match.";

        if (FileManager.usernameExists(username)) return "Username already taken. Choose another.";

        String studentId = "STU" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Student student = new Student(
                username.trim(),
                PasswordUtil.hashPassword(password),
                fullName.trim(),
                email.trim(),
                studentId
        );
        FileManager.saveUser(student);
        return null; // Success
    }

    /**
     * Change a user's password.
     *
     * @return null on success, or an error message
     */
    public String changePassword(String username, String currentPassword,
                                 String newPassword, String confirmNewPassword) {
        if (ValidationUtil.isNullOrBlank(currentPassword)) return "Current password is required.";
        if (ValidationUtil.isNullOrBlank(newPassword)) return "New password is required.";
        if (!ValidationUtil.isValidPassword(newPassword)) return "New password must be at least 6 characters.";
        if (!newPassword.equals(confirmNewPassword)) return "New passwords do not match.";

        Optional<User> userOpt = FileManager.findUserByUsername(username);
        if (userOpt.isEmpty()) return "User not found.";

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            return "Current password is incorrect.";
        }

        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        FileManager.saveUser(user);
        return null; // Success
    }

    /**
     * Reset a student's password (admin action).
     */
    public String adminResetPassword(String username, String newPassword) {
        Optional<User> userOpt = FileManager.findUserByUsername(username);
        if (userOpt.isEmpty()) return "User not found.";

        User user = userOpt.get();
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        FileManager.saveUser(user);
        return null;
    }
}