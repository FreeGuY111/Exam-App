package com.examapp.util;

import com.examapp.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * File-based persistence manager using JSON (Gson).
 * Handles all read/write operations for Users, Exams, and Results.
 */
public class FileManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();

    private FileManager() {}

    public static void initialize() {
        try {
            Files.createDirectories(Paths.get(Constants.DATA_DIR));
            Files.createDirectories(Paths.get(Constants.EXPORT_DIR));

            // Create files if they don't exist
            createIfAbsent(Constants.USERS_FILE, "[]");
            createIfAbsent(Constants.EXAMS_FILE, "[]");
            createIfAbsent(Constants.RESULTS_FILE, "[]");
        } catch (IOException e) {
            System.err.println("Failed to initialize data directory: " + e.getMessage());
        }
    }

    private static void createIfAbsent(String filePath, String defaultContent) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.writeString(path, defaultContent, StandardOpenOption.CREATE);
        }
    }

    // ======================== USER OPERATIONS ========================

    public static List<User> loadUsers() {
        try {
            String json = Files.readString(Paths.get(Constants.USERS_FILE));
            Type listType = new TypeToken<List<JsonObject>>() {}.getType();
            List<JsonObject> rawList = GSON.fromJson(json, listType);
            List<User> users = new ArrayList<>();
            if (rawList == null) return users;

            for (JsonObject obj : rawList) {
                String role = obj.has("role") ? obj.get("role").getAsString() : "STUDENT";
                if ("ADMIN".equals(role)) {
                    users.add(GSON.fromJson(obj, Admin.class));
                } else {
                    users.add(GSON.fromJson(obj, Student.class));
                }
            }
            return users;
        } catch (IOException | JsonParseException e) {
            System.err.println("Error loading users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        try {
            String json = GSON.toJson(users);
            Files.writeString(Paths.get(Constants.USERS_FILE), json,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static void saveUser(User user) {
        List<User> users = loadUsers();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }
        if (!found) {
            users.add(user);
        }
        saveUsers(users);
    }

    public static Optional<User> findUserByUsername(String username) {
        return loadUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public static boolean usernameExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    // ======================== EXAM OPERATIONS ========================

    public static List<Exam> loadExams() {
        try {
            String json = Files.readString(Paths.get(Constants.EXAMS_FILE));
            Type listType = new TypeToken<List<Exam>>() {}.getType();
            List<Exam> exams = GSON.fromJson(json, listType);
            return exams != null ? exams : new ArrayList<>();
        } catch (IOException | JsonParseException e) {
            System.err.println("Error loading exams: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveExams(List<Exam> exams) {
        try {
            String json = GSON.toJson(exams);
            Files.writeString(Paths.get(Constants.EXAMS_FILE), json,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving exams: " + e.getMessage());
        }
    }

    public static void saveExam(Exam exam) {
        List<Exam> exams = loadExams();
        boolean found = false;
        for (int i = 0; i < exams.size(); i++) {
            if (exams.get(i).getId().equals(exam.getId())) {
                exams.set(i, exam);
                found = true;
                break;
            }
        }
        if (!found) {
            exams.add(exam);
        }
        saveExams(exams);
    }

    public static void deleteExam(String examId) {
        List<Exam> exams = loadExams();
        exams.removeIf(e -> e.getId().equals(examId));
        saveExams(exams);
    }

    public static Optional<Exam> findExamById(String examId) {
        return loadExams().stream()
                .filter(e -> e.getId().equals(examId))
                .findFirst();
    }

    // ======================== RESULT OPERATIONS ========================

    public static List<ExamResult> loadResults() {
        try {
            String json = Files.readString(Paths.get(Constants.RESULTS_FILE));
            Type listType = new TypeToken<List<ExamResult>>() {}.getType();
            List<ExamResult> results = GSON.fromJson(json, listType);
            return results != null ? results : new ArrayList<>();
        } catch (IOException | JsonParseException e) {
            System.err.println("Error loading results: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveResults(List<ExamResult> results) {
        try {
            String json = GSON.toJson(results);
            Files.writeString(Paths.get(Constants.RESULTS_FILE), json,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }

    public static void saveResult(ExamResult result) {
        List<ExamResult> results = loadResults();
        boolean found = false;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getId().equals(result.getId())) {
                results.set(i, result);
                found = true;
                break;
            }
        }
        if (!found) {
            results.add(result);
        }
        saveResults(results);
    }

    public static List<ExamResult> getResultsForStudent(String studentUsername) {
        List<ExamResult> all = loadResults();
        List<ExamResult> studentResults = new ArrayList<>();
        for (ExamResult r : all) {
            if (r.getStudentUsername().equalsIgnoreCase(studentUsername)) {
                studentResults.add(r);
            }
        }
        return studentResults;
    }

    public static List<ExamResult> getResultsForExam(String examId) {
        List<ExamResult> all = loadResults();
        List<ExamResult> examResults = new ArrayList<>();
        for (ExamResult r : all) {
            if (r.getExamId().equals(examId)) {
                examResults.add(r);
            }
        }
        return examResults;
    }

    public static boolean hasStudentAttemptedExam(String studentUsername, String examId) {
        return loadResults().stream()
                .anyMatch(r -> r.getStudentUsername().equalsIgnoreCase(studentUsername)
                        && r.getExamId().equals(examId));
    }

    // ======================== EXPORT OPERATIONS ========================

    public static String exportResultToFile(ExamResult result) {
        try {
            String fileName = result.getStudentUsername() + "_" +
                    result.getExamTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_result.txt";
            String filePath = Constants.EXPORT_DIR + "/" + fileName;

            StringBuilder sb = new StringBuilder();
            sb.append("=".repeat(60)).append("\n");
            sb.append("           ONLINE EXAMINATION SYSTEM - RESULT\n");
            sb.append("=".repeat(60)).append("\n\n");
            sb.append("Student   : ").append(result.getStudentFullName()).append("\n");
            sb.append("Username  : ").append(result.getStudentUsername()).append("\n");
            sb.append("Exam      : ").append(result.getExamTitle()).append("\n");
            sb.append("Date      : ").append(result.getSubmittedAt() != null ?
                    result.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "N/A").append("\n");
            sb.append("Time Taken: ").append(result.getFormattedTime()).append("\n\n");
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("MCQ Score     : %d / %d\n", result.getMcqScore(), result.getTotalMCQMarks()));
            sb.append(String.format("Total Score   : %d / %d\n", result.getFinalScore(), result.getTotalMarks()));
            sb.append(String.format("Percentage    : %.1f%%\n", result.getPercentage()));
            sb.append(String.format("Result        : %s\n", result.isPassed() ? "PASS" : "FAIL"));
            sb.append("-".repeat(60)).append("\n\n");

            if (!result.getSubjectiveAnswers().isEmpty()) {
                sb.append("SUBJECTIVE ANSWERS:\n");
                sb.append("-".repeat(60)).append("\n");
                int idx = 1;
                for (Map.Entry<String, String> entry : result.getSubjectiveAnswers().entrySet()) {
                    sb.append("Q").append(idx++).append(": ").append(entry.getValue()).append("\n\n");
                }
            }

            sb.append("=".repeat(60)).append("\n");

            Files.writeString(Paths.get(filePath), sb.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return filePath;
        } catch (IOException e) {
            System.err.println("Export failed: " + e.getMessage());
            return null;
        }
    }

    // ======================== LOCAL DATE TIME ADAPTER ========================

    static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.format(FMT));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            try {
                return LocalDateTime.parse(json.getAsString(), FMT);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
    }
}