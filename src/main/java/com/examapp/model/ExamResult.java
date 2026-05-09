package com.examapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ExamResult stores a student's responses and computed score for a given exam attempt.
 */
public class ExamResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String examId;
    private String examTitle;
    private String studentUsername;
    private String studentFullName;

    // Map of questionId -> student's answer
    private Map<String, String> answers;

    private int totalQuestions;
    private int mcqScore;
    private int totalMCQMarks;
    private int subjectiveMarks;        // Subjective marks (manual or 0 default)
    private int totalMarks;
    private double percentage;
    private boolean passed;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private long timeTakenSeconds;
    private boolean autoSubmitted;      // True if timer expired

    // Subjective answers stored for admin review
    private Map<String, String> subjectiveAnswers;

    public ExamResult() {
        this.id = UUID.randomUUID().toString();
        this.answers = new HashMap<>();
        this.subjectiveAnswers = new HashMap<>();
        this.startedAt = LocalDateTime.now();
    }

    public ExamResult(String examId, String examTitle, String studentUsername, String studentFullName) {
        this();
        this.examId = examId;
        this.examTitle = examTitle;
        this.studentUsername = studentUsername;
        this.studentFullName = studentFullName;
    }

    public void recordAnswer(String questionId, String answer) {
        answers.put(questionId, answer);
    }

    public String getAnswer(String questionId) {
        return answers.getOrDefault(questionId, "");
    }

    public void recordSubjectiveAnswer(String questionId, String answer) {
        subjectiveAnswers.put(questionId, answer);
    }

    public void calculateScore(java.util.List<Question> questions) {
        this.totalQuestions = questions.size();
        this.mcqScore = 0;
        this.totalMCQMarks = 0;
        this.subjectiveMarks = 0;

        for (Question q : questions) {
            if (q.isMCQ()) {
                this.totalMCQMarks += q.getMarks();
                String studentAns = answers.get(q.getId());
                if (studentAns != null && q.checkAnswer(studentAns)) {
                    this.mcqScore += q.getMarks();
                }
            } else if (q.isSubjective()) {
                // Subjective: store answer but award 0 marks automatically
                String studentAns = answers.get(q.getId());
                if (studentAns != null && !studentAns.isBlank()) {
                    subjectiveAnswers.put(q.getId(), studentAns);
                }
            }
        }

        // Total scored = MCQ score (subjective = 0 auto, needs manual review in real system)
        int scoredMarks = this.mcqScore + this.subjectiveMarks;
        this.percentage = totalMarks > 0 ? ((double) scoredMarks / totalMarks) * 100.0 : 0.0;
        this.passed = percentage >= 50.0;
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public Map<String, String> getAnswers() { return answers; }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getMcqScore() { return mcqScore; }
    public void setMcqScore(int mcqScore) { this.mcqScore = mcqScore; }

    public int getTotalMCQMarks() { return totalMCQMarks; }
    public void setTotalMCQMarks(int totalMCQMarks) { this.totalMCQMarks = totalMCQMarks; }

    public int getSubjectiveMarks() { return subjectiveMarks; }
    public void setSubjectiveMarks(int subjectiveMarks) { this.subjectiveMarks = subjectiveMarks; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public long getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(long timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public boolean isAutoSubmitted() { return autoSubmitted; }
    public void setAutoSubmitted(boolean autoSubmitted) { this.autoSubmitted = autoSubmitted; }

    public Map<String, String> getSubjectiveAnswers() { return subjectiveAnswers; }
    public void setSubjectiveAnswers(Map<String, String> subjectiveAnswers) { this.subjectiveAnswers = subjectiveAnswers; }

    public int getFinalScore() { return mcqScore + subjectiveMarks; }

    public String getFormattedTime() {
        long minutes = timeTakenSeconds / 60;
        long seconds = timeTakenSeconds % 60;
        return String.format("%d min %d sec", minutes, seconds);
    }

    @Override
    public String toString() {
        return "ExamResult{id='" + id + "', student='" + studentUsername +
                "', exam='" + examTitle + "', score=" + getFinalScore() + "/" + totalMarks + "}";
    }
}