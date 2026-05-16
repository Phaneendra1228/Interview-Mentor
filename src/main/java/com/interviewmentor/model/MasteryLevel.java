package com.interviewmentor.model;

/**
 * Represents per-category mastery tracking for a user.
 * Maps to flowchart: "Check for Mastery & Plan Next",
 * "Update Profile & Session Progress".
 */
public class MasteryLevel {
    private int id;
    private int userId;
    private String category;
    private int level;               // 1=Novice, 2=Beginner, 3=Intermediate, 4=Advanced, 5=Master
    private int totalAttempted;
    private int totalCorrect;
    private double currentAccuracy;
    private boolean advanceRecommended;

    public MasteryLevel() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }

    public double getCurrentAccuracy() { return currentAccuracy; }
    public void setCurrentAccuracy(double currentAccuracy) { this.currentAccuracy = currentAccuracy; }

    public boolean isAdvanceRecommended() { return advanceRecommended; }
    public void setAdvanceRecommended(boolean advanceRecommended) { this.advanceRecommended = advanceRecommended; }

    /** Get human-readable level name */
    public String getLevelName() {
        return switch (level) {
            case 1 -> "Novice";
            case 2 -> "Beginner";
            case 3 -> "Intermediate";
            case 4 -> "Advanced";
            case 5 -> "Master";
            default -> "Unknown";
        };
    }

    /** Get the next difficulty to recommend based on mastery */
    public String getRecommendedDifficulty() {
        return switch (level) {
            case 1, 2 -> "Easy";
            case 3 -> "Medium";
            case 4, 5 -> "Hard";
            default -> "Mixed";
        };
    }
}
