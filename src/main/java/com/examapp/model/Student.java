package com.examapp.model;

import com.examapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Student user model with exam attempt tracking.
 */
public class Student extends User {

    private static final long serialVersionUID = 1L;

    private String studentId;
    private int totalExamsAttempted;
    private double averageScore;
    private List<String> attemptedExamIds;

    public Student() {
        super();
        setRole(Constants.ROLE_STUDENT);
        this.attemptedExamIds = new ArrayList<>();
    }

    public Student(String username, String passwordHash, String fullName, String email, String studentId) {
        super(username, passwordHash, fullName, email, Constants.ROLE_STUDENT);
        this.studentId = studentId;
        this.totalExamsAttempted = 0;
        this.averageScore = 0.0;
        this.attemptedExamIds = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getTotalExamsAttempted() {
        return totalExamsAttempted;
    }

    public void setTotalExamsAttempted(int totalExamsAttempted) {
        this.totalExamsAttempted = totalExamsAttempted;
    }

    public void incrementAttempts() {
        this.totalExamsAttempted++;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public List<String> getAttemptedExamIds() {
        if (attemptedExamIds == null) {
            attemptedExamIds = new ArrayList<>();
        }
        return attemptedExamIds;
    }

    public void setAttemptedExamIds(List<String> attemptedExamIds) {
        this.attemptedExamIds = attemptedExamIds;
    }

    public boolean hasAttemptedExam(String examId) {
        return getAttemptedExamIds().contains(examId);
    }

    public void recordAttempt(String examId) {
        if (!hasAttemptedExam(examId)) {
            getAttemptedExamIds().add(examId);
            incrementAttempts();
        }
    }

    @Override
    public String toString() {
        return "Student{username='" + getUsername() + "', studentId='" + studentId + "'}";
    }
}