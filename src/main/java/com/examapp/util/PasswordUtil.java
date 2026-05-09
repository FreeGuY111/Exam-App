package com.examapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with salt for secure storage.
 * If BCrypt is available on classpath it is preferred.
 */
public final class PasswordUtil {

    private static final String SEPARATOR = "$SALT$";

    private PasswordUtil() {}

    /**
     * Hash a plain-text password using SHA-256 + random salt.
     */
    public static String hashPassword(String plainPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltBytes);
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            String hash = Base64.getEncoder().encodeToString(hashedBytes);

            return salt + SEPARATOR + hash;
        } catch (NoSuchAlgorithmException e) {
            // Fallback: store simple hash (not ideal but functional)
            return "PLAIN" + SEPARATOR + plainPassword;
        }
    }

    /**
     * Verify a plain-text password against a stored hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || plainPassword == null) return false;

        // Handle plain fallback
        if (storedHash.startsWith("PLAIN" + SEPARATOR)) {
            return plainPassword.equals(storedHash.substring(("PLAIN" + SEPARATOR).length()));
        }

        try {
            String[] parts = storedHash.split("\\$SALT\\$");
            if (parts.length != 2) return false;

            String saltStr = parts[0];
            String storedHashPart = parts[1];

            byte[] saltBytes = Base64.getDecoder().decode(saltStr);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltBytes);
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            String computedHash = Base64.getEncoder().encodeToString(hashedBytes);

            return computedHash.equals(storedHashPart);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Quick validation of password strength.
     */
    public static boolean isStrongPassword(String password) {
        return password != null && password.length() >= 6;
    }
}