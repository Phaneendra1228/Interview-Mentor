package com.interviewmentor.model;

import java.time.LocalDateTime;

/**
 * Represents one completed or in-progress quiz session.
 */
public class QuizSession {
    private int id;
    private int userId;
    private String category;
    private String difficulty;
    private int totalQuestions;
    private int correctAnswers;
    private int timeTakenSeconds;
    private LocalDateTime startedAt;

    public QuizSession() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    /** Calculate accuracy as a percentage 0-100 */
    public double getAccuracy() {
        if (totalQuestions == 0) return 0;
        return (correctAnswers * 100.0) / totalQuestions;
    }
}
