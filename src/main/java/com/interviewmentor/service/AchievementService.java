package com.interviewmentor.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Achievement system — tracks and awards badges for milestones.
 */
public class AchievementService {

    private final DatabaseService db = DatabaseService.getInstance();

    // Achievement definitions
    public static final String[][] ACHIEVEMENTS = {
        {"first_quiz",     "First Steps",       "Complete your first quiz",                 "\u2605", "1"},
        {"five_quizzes",   "Getting Serious",   "Complete 5 quizzes",                       "\u25B2", "5"},
        {"ten_quizzes",    "Quiz Master",       "Complete 10 quizzes",                      "\u2605", "10"},
        {"twenty_quizzes", "Unstoppable",       "Complete 20 quizzes",                      "\u26A1", "20"},
        {"fifty_quizzes",  "Legend",            "Complete 50 quizzes",                      "\u25C6", "50"},
        {"perfect_score",  "Perfectionist",     "Score 100% on any quiz",                   "\u25CE", "1"},
        {"three_streak",   "On Fire",           "Maintain a 3-day streak",                  "\u25B2", "3"},
        {"seven_streak",   "Week Warrior",      "Maintain a 7-day streak",                  "\u2713", "7"},
        {"all_categories", "Explorer",          "Take a quiz in every category",            "\u25CB", "8"},
        {"hundred_qs",     "Century",           "Answer 100 questions total",               "\u2605", "100"},
        {"speed_demon",    "Speed Demon",       "Complete a quiz in under 2 minutes",       "\u23F1", "1"},
        {"bookworm",       "Bookworm",          "Bookmark 10 questions",                    "\u25A3", "10"},
    };

    /** Check and award any new achievements after a quiz. Returns newly earned ones. */
    public List<String[]> checkAndAward(int userId) {
        List<String[]> newlyEarned = new ArrayList<>();
        AnalyticsService analytics = new AnalyticsService();
        StreakService streakSvc = new StreakService();
        BookmarkService bookmarkSvc = new BookmarkService();

        int totalQuizzes = analytics.getTotalQuizCount(userId);
        int totalQuestions = analytics.getOverallStats(userId).getTotalAttempted();
        int currentStreak = streakSvc.getCurrentStreak(userId);
        int bookmarkCount = bookmarkSvc.getBookmarkCount(userId);

        // Check each achievement
        if (totalQuizzes >= 1) tryAward(userId, "first_quiz", newlyEarned);
        if (totalQuizzes >= 5) tryAward(userId, "five_quizzes", newlyEarned);
        if (totalQuizzes >= 10) tryAward(userId, "ten_quizzes", newlyEarned);
        if (totalQuizzes >= 20) tryAward(userId, "twenty_quizzes", newlyEarned);
        if (totalQuizzes >= 50) tryAward(userId, "fifty_quizzes", newlyEarned);
        if (currentStreak >= 3) tryAward(userId, "three_streak", newlyEarned);
        if (currentStreak >= 7) tryAward(userId, "seven_streak", newlyEarned);
        if (totalQuestions >= 100) tryAward(userId, "hundred_qs", newlyEarned);
        if (bookmarkCount >= 10) tryAward(userId, "bookworm", newlyEarned);

        // Check perfect score
        try {
            String sql = "SELECT COUNT(*) FROM quiz_sessions WHERE user_id = ? AND correct_answers = total_questions AND total_questions > 0";
            PreparedStatement ps = db.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.getInt(1) > 0) tryAward(userId, "perfect_score", newlyEarned);
            ps.close();
        } catch (SQLException ignored) {}

        // Check all categories
        try {
            String sql = "SELECT COUNT(DISTINCT category) FROM quiz_sessions WHERE user_id = ?";
            PreparedStatement ps = db.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.getInt(1) >= 8) tryAward(userId, "all_categories", newlyEarned);
            ps.close();
        } catch (SQLException ignored) {}

        // Check speed demon
        try {
            String sql = "SELECT COUNT(*) FROM quiz_sessions WHERE user_id = ? AND time_taken_seconds < 120 AND total_questions >= 5";
            PreparedStatement ps = db.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.getInt(1) > 0) tryAward(userId, "speed_demon", newlyEarned);
            ps.close();
        } catch (SQLException ignored) {}

        return newlyEarned;
    }

    private void tryAward(int userId, String achievementId, List<String[]> newlyEarned) {
        if (!hasAchievement(userId, achievementId)) {
            awardAchievement(userId, achievementId);
            for (String[] a : ACHIEVEMENTS) {
                if (a[0].equals(achievementId)) {
                    newlyEarned.add(a);
                    break;
                }
            }
        }
    }

    public void awardAchievement(int userId, String achievementId) {
        String sql = "INSERT OR IGNORE INTO achievements (user_id, achievement_id) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, achievementId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AchievementService] Award error: " + e.getMessage());
        }
    }

    public boolean hasAchievement(int userId, String achievementId) {
        String sql = "SELECT COUNT(*) FROM achievements WHERE user_id = ? AND achievement_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, achievementId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> getEarnedAchievements(int userId) {
        List<String> earned = new ArrayList<>();
        String sql = "SELECT achievement_id FROM achievements WHERE user_id = ? ORDER BY earned_at ASC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) earned.add(rs.getString("achievement_id"));
        } catch (SQLException ignored) {}
        return earned;
    }

    public int getEarnedCount(int userId) {
        return getEarnedAchievements(userId).size();
    }

    /** Find achievement definition by ID */
    public static String[] getDefinition(String id) {
        for (String[] a : ACHIEVEMENTS) {
            if (a[0].equals(id)) return a;
        }
        return null;
    }
}
