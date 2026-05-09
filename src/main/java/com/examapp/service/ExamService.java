package com.examapp.service;

import com.examapp.model.Exam;
import com.examapp.model.Question;
import com.examapp.util.FileManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for exam management operations.
 */
public class ExamService {

    private static ExamService instance;

    private ExamService() {
        ensureSampleExamExists();
    }

    public static ExamService getInstance() {
        if (instance == null) {
            instance = new ExamService();
        }
        return instance;
    }

    /**
     * Creates a built-in sample exam with 20 questions (Java/CS knowledge).
     */
    private void ensureSampleExamExists() {
        List<Exam> exams = FileManager.loadExams();
        if (!exams.isEmpty()) return;

        Exam sampleExam = new Exam(
                "Java Programming Fundamentals",
                "A comprehensive test covering core Java programming concepts.",
                "admin",
                30
        );
        sampleExam.setRandomizeQuestions(true);

        // 18 MCQ questions
        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which keyword is used to define a class in Java?",
                "class", "define", "struct", "object", "A", 1, 0));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is the default value of an int variable in Java?",
                "0", "null", "undefined", "-1", "A", 1, 1));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which of the following is NOT a primitive type in Java?",
                "int", "boolean", "String", "double", "C", 1, 2));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What does JVM stand for?",
                "Java Virtual Machine", "Java Variable Method", "Java Verified Module", "Just Visual Machine", "A", 1, 3));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which method is the entry point of a Java application?",
                "start()", "run()", "main()", "init()", "C", 1, 4));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which access modifier makes a member accessible only within its own class?",
                "public", "protected", "default", "private", "D", 1, 5));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is inheritance in Java?",
                "A class using methods of another class",
                "A class inheriting properties and behaviors from another class",
                "Hiding implementation details",
                "Running multiple threads simultaneously", "B", 1, 6));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which collection does NOT allow duplicate elements?",
                "ArrayList", "LinkedList", "HashMap", "HashSet", "D", 1, 7));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is the size of an int in Java?",
                "16 bits", "32 bits", "64 bits", "8 bits", "B", 1, 8));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which keyword prevents a class from being subclassed?",
                "static", "abstract", "final", "sealed", "C", 1, 9));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What does the 'static' keyword mean for a method?",
                "The method cannot be overridden",
                "The method belongs to the class, not instances",
                "The method returns a constant",
                "The method is thread-safe", "B", 1, 10));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which exception is thrown when dividing by zero in Java?",
                "NullPointerException", "IllegalArgumentException",
                "ArithmeticException", "NumberFormatException", "C", 1, 11));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is an interface in Java?",
                "A concrete class with all implemented methods",
                "An abstract blueprint that a class must implement",
                "A data type for storing objects",
                "A method for sorting arrays", "B", 1, 12));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which operator is used to create a new object in Java?",
                "create", "new", "make", "alloc", "B", 1, 13));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is polymorphism?",
                "Multiple classes in one file",
                "One method name with multiple implementations",
                "A class with multiple constructors",
                "Wrapping code in a class", "B", 1, 14));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "Which loop guarantees at least one execution?",
                "for loop", "while loop", "do-while loop", "for-each loop", "C", 1, 15));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "How do you convert a String '123' to an integer in Java?",
                "String.toInt('123')", "Integer.parseInt('123')",
                "Int.parse('123')", "(int) '123'", "B", 1, 16));

        sampleExam.addQuestion(Question.createMCQ(sampleExam.getId(),
                "What is the correct way to declare an array of 5 integers?",
                "int arr = new int[5]", "int[] arr = new int[5]",
                "int arr[5]", "array<int> arr(5)", "B", 1, 17));

        // 2 Subjective questions
        sampleExam.addQuestion(Question.createSubjective(sampleExam.getId(),
                "Explain the concept of Object-Oriented Programming (OOP) and list its four main pillars with brief explanations.",
                2, 18));

        sampleExam.addQuestion(Question.createSubjective(sampleExam.getId(),
                "What is the difference between an abstract class and an interface in Java? Provide examples of when you would use each.",
                2, 19));

        FileManager.saveExam(sampleExam);
    }

    public List<Exam> getAllExams() {
        return FileManager.loadExams();
    }

    public List<Exam> getActiveExams() {
        return FileManager.loadExams().stream()
                .filter(Exam::isActive)
                .collect(Collectors.toList());
    }

    public Optional<Exam> getExamById(String examId) {
        return FileManager.findExamById(examId);
    }

    public void saveExam(Exam exam) {
        FileManager.saveExam(exam);
    }

    public void deleteExam(String examId) {
        FileManager.deleteExam(examId);
    }

    public void toggleExamActive(String examId) {
        Optional<Exam> opt = FileManager.findExamById(examId);
        opt.ifPresent(exam -> {
            exam.setActive(!exam.isActive());
            FileManager.saveExam(exam);
        });
    }

    public void addQuestionToExam(String examId, Question question) {
        Optional<Exam> opt = FileManager.findExamById(examId);
        opt.ifPresent(exam -> {
            exam.addQuestion(question);
            FileManager.saveExam(exam);
        });
    }

    public void removeQuestionFromExam(String examId, String questionId) {
        Optional<Exam> opt = FileManager.findExamById(examId);
        opt.ifPresent(exam -> {
            exam.removeQuestion(questionId);
            FileManager.saveExam(exam);
        });
    }
}