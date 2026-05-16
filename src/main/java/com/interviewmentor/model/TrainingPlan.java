package com.interviewmentor.model;

import java.time.LocalDateTime;

/**
 * Represents a Training Plan / Session Goal.
 * Maps to flowchart: "Set Goal / Fetch Session", "Finalize Plan",
 * "Training Action Plan", "Check for Mastery & Plan Next".
 */
public class TrainingPlan {
    private int id;
    private int userId;
    private String category;
    private String goal;            // User-defined session goal
    private String difficulty;
    private int targetScore;         // Target accuracy %
    private int achievedScore;       // Actual achieved accuracy %
    private boolean goalMet;
    private String actionPlan;       // Generated recommendations text
    private String status;           // ACTIVE, COMPLETED, NEEDS_RETRY
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Mock interview parameters
    private boolean isMockInterview;
    private int mockTimeLimitSeconds;
    private String mockFormat;       // "TIMED", "RELAXED", "STRICT"
    private boolean realTimeFeedback;
    private boolean voiceCaptureEnabled;
    private boolean extrasEnabled;   // AI skill optimization

    public TrainingPlan() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTargetScore() { return targetScore; }
    public void setTargetScore(int targetScore) { this.targetScore = targetScore; }

    public int getAchievedScore() { return achievedScore; }
    public void setAchievedScore(int achievedScore) { this.achievedScore = achievedScore; }

    public boolean isGoalMet() { return goalMet; }
    public void setGoalMet(boolean goalMet) { this.goalMet = goalMet; }

    public String getActionPlan() { return actionPlan; }
    public void setActionPlan(String actionPlan) { this.actionPlan = actionPlan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isMockInterview() { return isMockInterview; }
    public void setMockInterview(boolean mockInterview) { isMockInterview = mockInterview; }

    public int getMockTimeLimitSeconds() { return mockTimeLimitSeconds; }
    public void setMockTimeLimitSeconds(int mockTimeLimitSeconds) { this.mockTimeLimitSeconds = mockTimeLimitSeconds; }

    public String getMockFormat() { return mockFormat; }
    public void setMockFormat(String mockFormat) { this.mockFormat = mockFormat; }

    public boolean isRealTimeFeedback() { return realTimeFeedback; }
    public void setRealTimeFeedback(boolean realTimeFeedback) { this.realTimeFeedback = realTimeFeedback; }

    public boolean isVoiceCaptureEnabled() { return voiceCaptureEnabled; }
    public void setVoiceCaptureEnabled(boolean voiceCaptureEnabled) { this.voiceCaptureEnabled = voiceCaptureEnabled; }

    public boolean isExtrasEnabled() { return extrasEnabled; }
    public void setExtrasEnabled(boolean extrasEnabled) { this.extrasEnabled = extrasEnabled; }
}
