package com.interviewmentor.service;

import java.sql.*;
import java.time.LocalDate;

/**
 * Tracks daily quiz activity and calculates streaks.
 */
public class StreakService {

    private final DatabaseService db = DatabaseService.getInstance();

    /** Record activity for today (increment quiz count and questions answered). */
    public void recordActivity(int userId, int questionsAnswered) {
        String today = LocalDate.now().toString();
        String checkSql = "SELECT id, quiz_count, questions_answered FROM daily_activity WHERE user_id = ? AND activity_date = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Update existing record
                int id = rs.getInt("id");
                int qc = rs.getInt("quiz_count");
                int qa = rs.getInt("questions_answered");
                String updateSql = "UPDATE daily_activity SET quiz_count = ?, questions_answered = ? WHERE id = ?";
                try (PreparedStatement ups = db.getConnection().prepareStatement(updateSql)) {
                    ups.setInt(1, qc + 1);
                    ups.setInt(2, qa + questionsAnswered);
                    ups.setInt(3, id);
                    ups.executeUpdate();
                }
            } else {
                // Insert new record
                String insertSql = "INSERT INTO daily_activity (user_id, activity_date, quiz_count, questions_answered) VALUES (?, ?, 1, ?)";
                try (PreparedStatement ins = db.getConnection().prepareStatement(insertSql)) {
                    ins.setInt(1, userId);
                    ins.setString(2, today);
                    ins.setInt(3, questionsAnswered);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] Record error: " + e.getMessage());
        }
    }

    /** Get quiz count for the last 7 days (index 0 = 6 days ago, index 6 = today). */
    public int[] getLast7DaysActivity(int userId) {
        int[] activity = new int[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            String sql = "SELECT quiz_count FROM daily_activity WHERE user_id = ? AND activity_date = ?";
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, date.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    activity[i] = rs.getInt("quiz_count");
                }
            } catch (SQLException e) {
                // ignore
            }
        }
        return activity;
    }

    /** Calculate current consecutive day streak (including today). */
    public int getCurrentStreak(int userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        while (true) {
            String sql = "SELECT COUNT(*) FROM daily_activity WHERE user_id = ? AND activity_date = ? AND quiz_count > 0";
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, date.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.getInt(1) > 0) {
                    streak++;
                    date = date.minusDays(1);
                } else {
                    break;
                }
            } catch (SQLException e) {
                break;
            }
        }
        return streak;
    }

    /** Calculate longest ever streak for a user. */
    public int getLongestStreak(int userId) {
        String sql = "SELECT activity_date FROM daily_activity WHERE user_id = ? AND quiz_count > 0 ORDER BY activity_date ASC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            int longest = 0;
            int current = 0;
            LocalDate prev = null;
            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("activity_date"));
                if (prev != null && date.equals(prev.plusDays(1))) {
                    current++;
                } else {
                    current = 1;
                }
                longest = Math.max(longest, current);
                prev = date;
            }
            return longest;
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Get last N quiz scores for a user (most recent first). */
    public double[] getRecentScores(int userId, int count) {
        String sql = "SELECT correct_answers * 100.0 / total_questions AS accuracy FROM quiz_sessions WHERE user_id = ? AND total_questions > 0 ORDER BY started_at DESC LIMIT ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, count);
            ResultSet rs = ps.executeQuery();
            java.util.List<Double> scores = new java.util.ArrayList<>();
            while (rs.next()) {
                scores.add(rs.getDouble("accuracy"));
            }
            // Reverse so oldest is first
            double[] result = new double[scores.size()];
            for (int i = 0; i < scores.size(); i++) {
                result[i] = scores.get(scores.size() - 1 - i);
            }
            return result;
        } catch (SQLException e) {
            return new double[0];
        }
    }
    /** Get activity data for heatmap: map of date string -> quiz count, for last N days. */
    public java.util.Map<String, Integer> getActivityHeatmap(int userId, int days) {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        String sql = "SELECT activity_date, quiz_count FROM daily_activity WHERE user_id = ? AND activity_date >= ? ORDER BY activity_date";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today.minusDays(days).toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("activity_date"), rs.getInt("quiz_count"));
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] Heatmap error: " + e.getMessage());
        }
        return data;
    }
}
