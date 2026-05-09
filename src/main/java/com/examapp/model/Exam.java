package com.examapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Exam model containing all metadata and questions for a test.
 */
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String createdBy;           // Admin username
    private int durationMinutes;
    private boolean randomizeQuestions;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalMarks;
    private int passingMarks;
    private List<Question> questions;

    public Exam() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.randomizeQuestions = false;
        this.questions = new ArrayList<>();
        this.durationMinutes = 30;
    }

    public Exam(String title, String description, String createdBy, int durationMinutes) {
        this();
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.durationMinutes = durationMinutes;
    }

    public void addQuestion(Question question) {
        question.setExamId(this.id);
        question.setOrderIndex(questions.size());
        questions.add(question);
        recalculateTotals();
    }

    public void removeQuestion(String questionId) {
        questions.removeIf(q -> q.getId().equals(questionId));
        recalculateTotals();
    }

    private void recalculateTotals() {
        this.totalMarks = questions.stream().mapToInt(Question::getMarks).sum();
        this.passingMarks = (int) Math.ceil(totalMarks * 0.5);
    }

    public List<Question> getQuestionsForExam() {
        List<Question> examQuestions = new ArrayList<>(questions);
        if (randomizeQuestions) {
            Collections.shuffle(examQuestions);
        }
        return examQuestions;
    }

    public int getMCQCount() {
        return (int) questions.stream().filter(Question::isMCQ).count();
    }

    public int getSubjectiveCount() {
        return (int) questions.stream().filter(Question::isSubjective).count();
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isRandomizeQuestions() { return randomizeQuestions; }
    public void setRandomizeQuestions(boolean randomizeQuestions) { this.randomizeQuestions = randomizeQuestions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getPassingMarks() { return passingMarks; }
    public void setPassingMarks(int passingMarks) { this.passingMarks = passingMarks; }

    public List<Question> getQuestions() {
        if (questions == null) questions = new ArrayList<>();
        return questions;
    }
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        recalculateTotals();
    }

    @Override
    public String toString() {
        return "Exam{id='" + id + "', title='" + title + "', questions=" + questions.size() + "}";
    }
}