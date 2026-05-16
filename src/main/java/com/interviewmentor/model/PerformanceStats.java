package com.interviewmentor.model;

/**
 * Aggregated performance statistics for a single category.
 */
public class PerformanceStats {
    private String category;
    private int totalAttempted;
    private int correctCount;
    private double avgTimeSeconds;
    private double accuracy;

    public PerformanceStats() {}

    public PerformanceStats(String category, int totalAttempted, int correctCount,
                            double avgTimeSeconds) {
        this.category = category;
        this.totalAttempted = totalAttempted;
        this.correctCount = correctCount;
        this.avgTimeSeconds = avgTimeSeconds;
        this.accuracy = totalAttempted > 0 ? (correctCount * 100.0 / totalAttempted) : 0;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public double getAvgTimeSeconds() { return avgTimeSeconds; }
    public void setAvgTimeSeconds(double avgTimeSeconds) { this.avgTimeSeconds = avgTimeSeconds; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    /** Returns true if accuracy is below 50% and the user has attempted at least 3 questions */
    public boolean isWeakArea() {
        return totalAttempted >= 3 && accuracy < 50.0;
    }
}
