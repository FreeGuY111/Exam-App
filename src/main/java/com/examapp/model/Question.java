package com.examapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Question model supporting both MCQ and Subjective types.
 */
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String examId;
    private QuestionType type;
    private String questionText;
    private List<String> options;        // For MCQ: 4 options (A, B, C, D)
    private String correctAnswer;        // For MCQ: "A", "B", "C", or "D"
    private int marks;
    private int orderIndex;
    private int timeLimitSeconds;        // Per-question timer (0 = no limit)

    public Question() {
        this.id = UUID.randomUUID().toString();
        this.options = new ArrayList<>();
        this.marks = 1;
        this.timeLimitSeconds = 0;
    }

    public Question(QuestionType type, String questionText, int marks) {
        this();
        this.type = type;
        this.questionText = questionText;
        this.marks = marks;
    }

    // Static factory for MCQ
    public static Question createMCQ(String examId, String questionText,
                                     String optionA, String optionB,
                                     String optionC, String optionD,
                                     String correctAnswer, int marks, int order) {
        Question q = new Question(QuestionType.MCQ, questionText, marks);
        q.setExamId(examId);
        q.getOptions().add(optionA);
        q.getOptions().add(optionB);
        q.getOptions().add(optionC);
        q.getOptions().add(optionD);
        q.setCorrectAnswer(correctAnswer);
        q.setOrderIndex(order);
        return q;
    }

    // Static factory for Subjective
    public static Question createSubjective(String examId, String questionText, int marks, int order) {
        Question q = new Question(QuestionType.SUBJECTIVE, questionText, marks);
        q.setExamId(examId);
        q.setOrderIndex(order);
        return q;
    }

    public boolean isMCQ() {
        return QuestionType.MCQ.equals(type);
    }

    public boolean isSubjective() {
        return QuestionType.SUBJECTIVE.equals(type);
    }

    public boolean checkAnswer(String studentAnswer) {
        if (isMCQ() && correctAnswer != null && studentAnswer != null) {
            return correctAnswer.trim().equalsIgnoreCase(studentAnswer.trim());
        }
        return false; // Subjective requires manual evaluation (auto 0 for now)
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() {
        if (options == null) options = new ArrayList<>();
        return options;
    }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public String getOptionA() { return options != null && options.size() > 0 ? options.get(0) : ""; }
    public String getOptionB() { return options != null && options.size() > 1 ? options.get(1) : ""; }
    public String getOptionC() { return options != null && options.size() > 2 ? options.get(2) : ""; }
    public String getOptionD() { return options != null && options.size() > 3 ? options.get(3) : ""; }

    @Override
    public String toString() {
        return "Question{id='" + id + "', type=" + type + ", text='" + questionText + "'}";
    }
}