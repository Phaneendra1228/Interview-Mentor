package com.interviewmentor.service;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes performance analytics: category accuracy, weak areas, overall stats.
 */
public class AnalyticsService {

    private final DatabaseService db = DatabaseService.getInstance();

    /**
     * Get per-category performance stats for a user.
     */
    public List<PerformanceStats> getCategoryStats(int userId) {
        List<PerformanceStats> statsList = new ArrayList<>();

        for (Category cat : Category.values()) {
            String sql = """
                SELECT
                    COALESCE(SUM(total_questions), 0) AS total_attempted,
                    COALESCE(SUM(correct_answers), 0) AS total_correct,
                    CASE WHEN COUNT(*) > 0 THEN COALESCE(AVG(time_taken_seconds * 1.0 / NULLIF(total_questions, 0)), 0) ELSE 0 END AS avg_time
                FROM quiz_sessions
                WHERE user_id = ? AND category = ?
            """;
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, cat.name());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int totalAttempted = rs.getInt("total_attempted");
                    int totalCorrect = rs.getInt("total_correct");
                    double avgTime = rs.getDouble("avg_time");
                    statsList.add(new PerformanceStats(cat.name(), totalAttempted, totalCorrect, avgTime));
                }
            } catch (SQLException e) {
                System.err.println("[Analytics] Error: " + e.getMessage());
            }
        }
        return statsList;
    }

    /**
     * Get overall stats across all categories.
     */
    public PerformanceStats getOverallStats(int userId) {
        String sql = """
            SELECT
                COALESCE(SUM(total_questions), 0) AS total_attempted,
                COALESCE(SUM(correct_answers), 0) AS total_correct,
                CASE WHEN COUNT(*) > 0 THEN AVG(time_taken_seconds) ELSE 0 END AS avg_time
            FROM quiz_sessions WHERE user_id = ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PerformanceStats("OVERALL",
                    rs.getInt("total_attempted"),
                    rs.getInt("total_correct"),
                    rs.getDouble("avg_time"));
            }
        } catch (SQLException e) {
            System.err.println("[Analytics] Error: " + e.getMessage());
        }
        return new PerformanceStats("OVERALL", 0, 0, 0);
    }

    /**
     * Get total number of quiz sessions for a user.
     */
    public int getTotalQuizCount(int userId) {
        String sql = "SELECT COUNT(*) FROM quiz_sessions WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Find the user's strongest category (highest accuracy with min 1 attempt).
     */
    public String getBestCategory(int userId) {
        List<PerformanceStats> stats = getCategoryStats(userId);
        String best = "N/A";
        double bestAcc = -1;
        for (PerformanceStats s : stats) {
            if (s.getTotalAttempted() > 0 && s.getAccuracy() > bestAcc) {
                bestAcc = s.getAccuracy();
                Category cat = Category.fromString(s.getCategory());
                best = cat != null ? cat.getDisplayName() : s.getCategory();
            }
        }
        return best;
    }

    /**
     * Get categories where accuracy < 50% (weak areas).
     */
    public List<PerformanceStats> getWeakAreas(int userId) {
        List<PerformanceStats> weak = new ArrayList<>();
        for (PerformanceStats s : getCategoryStats(userId)) {
            if (s.isWeakArea()) {
                weak.add(s);
            }
        }
        return weak;
    }

    /**
     * Get per-session accuracy history (date, accuracy%) for trend charts.
     * Returns up to 'limit' most recent sessions.
     */
    public List<double[]> getSessionAccuracyHistory(int userId, int limit) {
        List<double[]> list = new ArrayList<>();
        String sql = """
            SELECT started_at, total_questions, correct_answers
            FROM quiz_sessions WHERE user_id = ? AND total_questions > 0
            ORDER BY started_at ASC
            LIMIT ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            int index = 0;
            while (rs.next()) {
                int total = rs.getInt("total_questions");
                int correct = rs.getInt("correct_answers");
                double acc = total > 0 ? (correct * 100.0 / total) : 0;
                list.add(new double[]{index++, acc});
            }
        } catch (SQLException e) {
            System.err.println("[Analytics] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get difficulty distribution: counts for Easy, Medium, Hard quizzes.
     */
    public int[] getDifficultyDistribution(int userId) {
        int[] counts = new int[3]; // [Easy, Medium, Hard]
        String sql = "SELECT difficulty, COUNT(*) as cnt FROM quiz_sessions WHERE user_id = ? AND difficulty IS NOT NULL GROUP BY difficulty";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String d = rs.getString("difficulty");
                int c = rs.getInt("cnt");
                if ("Easy".equalsIgnoreCase(d)) counts[0] = c;
                else if ("Medium".equalsIgnoreCase(d)) counts[1] = c;
                else if ("Hard".equalsIgnoreCase(d)) counts[2] = c;
            }
        } catch (SQLException e) {
            System.err.println("[Analytics] Error: " + e.getMessage());
        }
        return counts;
    }

    /**
     * Get average accuracy per difficulty level.
     */
    public double[] getDifficultyAccuracy(int userId) {
        double[] acc = new double[3]; // [Easy, Medium, Hard]
        String sql = """
            SELECT difficulty,
                   CASE WHEN SUM(total_questions) > 0 
                        THEN SUM(correct_answers) * 100.0 / SUM(total_questions) ELSE 0 END AS accuracy
            FROM quiz_sessions WHERE user_id = ? AND difficulty IS NOT NULL
            GROUP BY difficulty
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String d = rs.getString("difficulty");
                double a = rs.getDouble("accuracy");
                if ("Easy".equalsIgnoreCase(d)) acc[0] = a;
                else if ("Medium".equalsIgnoreCase(d)) acc[1] = a;
                else if ("Hard".equalsIgnoreCase(d)) acc[2] = a;
            }
        } catch (SQLException e) {
            System.err.println("[Analytics] Error: " + e.getMessage());
        }
        return acc;
    }

    /**
     * Get total time spent studying (sum of all time_taken_seconds).
     */
    public int getTotalStudyTime(int userId) {
        String sql = "SELECT COALESCE(SUM(time_taken_seconds), 0) FROM quiz_sessions WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
}

