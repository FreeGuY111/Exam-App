package com.examapp.model;

/**
 * Enum representing the type of question in an exam.
 */
public enum QuestionType {
    MCQ("Multiple Choice"),
    SUBJECTIVE("Subjective");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}