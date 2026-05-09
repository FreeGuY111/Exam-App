package com.examapp.service;

import com.examapp.model.ExamResult;
import com.examapp.util.FileManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service class for exam result operations.
 * Singleton - call ResultService.getInstance() everywhere.
 */
public class ResultService {

    private static ResultService instance;

    private ResultService() {}

    public static ResultService getInstance() {
        if (instance == null) {
            instance = new ResultService();
        }
        return instance;
    }

    /**
     * Persist a new result (or update existing one with same id).
     */
    public void saveResult(ExamResult result) {
        if (result == null) return;
        FileManager.saveResult(result);
    }

    /**
     * Return ALL results sorted newest-first.
     */
    public List<ExamResult> getAllResults() {
        List<ExamResult> results = FileManager.loadResults();
        results.sort(
                Comparator.comparing(
                        (ExamResult r) -> r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : "",
                        Comparator.reverseOrder()
                )
        );
        return results;
    }

    /**
     * Return all results for one student, newest-first.
     */
    public List<ExamResult> getStudentResults(String studentUsername) {
        List<ExamResult> results = FileManager.getResultsForStudent(studentUsername);
        results.sort(
                Comparator.comparing(
                        (ExamResult r) -> r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : "",
                        Comparator.reverseOrder()
                )
        );
        return results;
    }

    /**
     * Return all results for one exam.
     */
    public List<ExamResult> getExamResults(String examId) {
        return FileManager.getResultsForExam(examId);
    }

    /**
     * Check whether a student has already submitted a result for a given exam.
     */
    public boolean hasAttempted(String studentUsername, String examId) {
        return FileManager.hasStudentAttemptedExam(studentUsername, examId);
    }

    /**
     * Find one specific result by its id.
     */
    public Optional<ExamResult> findById(String resultId) {
        return FileManager.loadResults().stream()
                .filter(r -> r.getId().equals(resultId))
                .findFirst();
    }

    /**
     * Export a result to a plain-text file.
     * Returns the full file path on success, null on failure.
     */
    public String exportResult(ExamResult result) {
        if (result == null) return null;
        return FileManager.exportResultToFile(result);
    }

    /**
     * Count how many times a student has attempted exams in total.
     */
    public int getTotalAttemptsForStudent(String studentUsername) {
        return FileManager.getResultsForStudent(studentUsername).size();
    }

    /**
     * Calculate average percentage for a student across all attempts.
     */
    public double getAveragePercentageForStudent(String studentUsername) {
        List<ExamResult> results = FileManager.getResultsForStudent(studentUsername);
        if (results.isEmpty()) return 0.0;
        return results.stream()
                .mapToDouble(ExamResult::getPercentage)
                .average()
                .orElse(0.0);
    }
}