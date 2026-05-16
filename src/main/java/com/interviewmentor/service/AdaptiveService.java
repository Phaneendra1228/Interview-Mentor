package com.interviewmentor.service;

import com.interviewmentor.model.MasteryLevel;
import com.interviewmentor.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptive difficulty and skill optimization service.
 * Maps to flowchart: "Advance Session Needed?", "Analyze Skills & Optimize List",
 * "Check for Mastery & Plan Next", "Update Profile & Session Progress",
 * "Handle Missed Interview Questions", "Improvement Possible?".
 */
public class AdaptiveService {

    private final DatabaseService db = DatabaseService.getInstance();
    private final QuestionService questionService = new QuestionService();

    /**
     * Check if user should advance to next difficulty level.
     * Flowchart: "Advance Session Needed?"
     */
    public boolean isAdvanceNeeded(int userId, String category) {
        MasteryLevel mastery = getMastery(userId, category);
        if (mastery == null) return false;
        // Advance if accuracy >= 75% with at least 10 questions attempted
        return mastery.getCurrentAccuracy() >= 75.0 && mastery.getTotalAttempted() >= 10;
    }

    /**
     * Get or create mastery level for a user/category.
     * Flowchart: "Check for Mastery & Plan Next"
     */
    public MasteryLevel getMastery(int userId, String category) {
        String sql = "SELECT * FROM mastery_tracking WHERE user_id = ? AND category = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapMastery(rs);
        } catch (SQLException e) {
            System.err.println("[AdaptiveService] Query error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all mastery levels for a user.
     */
    public List<MasteryLevel> getAllMasteryLevels(int userId) {
        String sql = "SELECT * FROM mastery_tracking WHERE user_id = ? ORDER BY category";
        List<MasteryLevel> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMastery(rs));
        } catch (SQLException e) {
            System.err.println("[AdaptiveService] Query error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Update mastery after a quiz session.
     * Flowchart: "Update Profile & Session Progress"
     */
    public MasteryLevel updateMastery(int userId, String category, int questionsAttempted,
                                       int correctAnswers) {
        MasteryLevel mastery = getMastery(userId, category);
        if (mastery == null) {
            // Create new mastery record
            String sql = """
                INSERT INTO mastery_tracking (user_id, category, level,
                    total_attempted, total_correct, current_accuracy, advance_recommended)
                VALUES (?, ?, 1, ?, ?, ?, ?)
            """;
            double acc = questionsAttempted > 0 ? (correctAnswers * 100.0 / questionsAttempted) : 0;
            boolean advance = acc >= 75.0 && questionsAttempted >= 10;
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, category);
                ps.setInt(3, questionsAttempted);
                ps.setInt(4, correctAnswers);
                ps.setDouble(5, acc);
                ps.setInt(6, advance ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[AdaptiveService] Insert error: " + e.getMessage());
            }
        } else {
            // Update existing
            int newTotal = mastery.getTotalAttempted() + questionsAttempted;
            int newCorrect = mastery.getTotalCorrect() + correctAnswers;
            double newAcc = newTotal > 0 ? (newCorrect * 100.0 / newTotal) : 0;
            int newLevel = calculateLevel(newAcc, newTotal);
            boolean advance = newAcc >= 75.0 && newTotal >= 10 && newLevel > mastery.getLevel();

            String sql = """
                UPDATE mastery_tracking SET total_attempted = ?, total_correct = ?,
                    current_accuracy = ?, level = ?, advance_recommended = ?
                WHERE user_id = ? AND category = ?
            """;
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, newTotal);
                ps.setInt(2, newCorrect);
                ps.setDouble(3, newAcc);
                ps.setInt(4, newLevel);
                ps.setInt(5, advance ? 1 : 0);
                ps.setInt(6, userId);
                ps.setString(7, category);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[AdaptiveService] Update error: " + e.getMessage());
            }
        }
        return getMastery(userId, category);
    }

    /**
     * Analyze skills and optimize question list.
     * Flowchart: "Analyze Skills & Optimize List"
     * Returns questions prioritized by weak areas.
     */
    public List<Question> getOptimizedQuestions(int userId, String category,
                                                 String difficulty, int count) {
        // First, get questions the user has gotten wrong most recently
        List<Integer> missedIds = getMissedQuestionIds(userId, category, count);

        List<Question> optimized = new ArrayList<>();
        // Add missed questions first (review mode)
        for (int id : missedIds) {
            if (optimized.size() >= count / 2) break; // At most half from missed
            Question q = questionService.getQuestionById(id);
            if (q != null) optimized.add(q);
        }

        // Fill remaining with random questions
        int remaining = count - optimized.size();
        if (remaining > 0) {
            List<Question> random = questionService.getRandomQuestions(category, difficulty, remaining + 10);
            for (Question q : random) {
                if (optimized.size() >= count) break;
                boolean alreadyAdded = optimized.stream().anyMatch(oq -> oq.getId() == q.getId());
                if (!alreadyAdded) optimized.add(q);
            }
        }

        return optimized;
    }

    /**
     * Get missed question IDs for a category.
     * Flowchart: "Handle Missed Interview Questions"
     */
    public List<Integer> getMissedQuestionIds(int userId, String category, int limit) {
        String sql = """
            SELECT DISTINCT qr.question_id FROM quiz_results qr
            JOIN quiz_sessions qs ON qr.session_id = qs.id
            WHERE qs.user_id = ? AND qs.category = ? AND qr.is_correct = 0
            ORDER BY qr.id DESC LIMIT ?
        """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("question_id"));
        } catch (SQLException e) {
            System.err.println("[AdaptiveService] Missed query error: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Get missed questions as Question objects.
     * Flowchart: "Handle Missed Interview Questions"
     */
    public List<Question> getMissedQuestions(int userId, String category, int limit) {
        List<Integer> ids = getMissedQuestionIds(userId, category, limit);
        List<Question> questions = new ArrayList<>();
        for (int id : ids) {
            Question q = questionService.getQuestionById(id);
            if (q != null) questions.add(q);
        }
        return questions;
    }

    /**
     * Get all missed questions across all categories.
     */
    public List<Question> getAllMissedQuestions(int userId, int limit) {
        String sql = """
            SELECT DISTINCT qr.question_id FROM quiz_results qr
            JOIN quiz_sessions qs ON qr.session_id = qs.id
            WHERE qs.user_id = ? AND qr.is_correct = 0
            ORDER BY qr.id DESC LIMIT ?
        """;
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question q = questionService.getQuestionById(rs.getInt("question_id"));
                if (q != null) questions.add(q);
            }
        } catch (SQLException e) {
            System.err.println("[AdaptiveService] Missed query error: " + e.getMessage());
        }
        return questions;
    }

    /**
     * Check if improvement is possible based on recent performance.
     * Flowchart: "Improvement Possible?"
     */
    public boolean isImprovementPossible(int userId, String category) {
        MasteryLevel mastery = getMastery(userId, category);
        if (mastery == null) return true; // No data = can improve
        return mastery.getCurrentAccuracy() < 100.0;
    }

    /**
     * Get the recommended next step for a user.
     * Flowchart: "Check for Mastery & Plan Next"
     */
    public String getNextStepRecommendation(int userId, String category) {
        MasteryLevel mastery = getMastery(userId, category);
        if (mastery == null) {
            return "Start your first quiz in this category to begin tracking mastery!";
        }

        if (mastery.getCurrentAccuracy() >= 90 && mastery.getTotalAttempted() >= 20) {
            return "[MASTERY] You've achieved mastery! Try mock interviews or help others practice.";
        } else if (mastery.isAdvanceRecommended()) {
            return "[ADVANCE] Ready to advance! Try harder difficulty or timed mock sessions.";
        } else if (mastery.getCurrentAccuracy() >= 70) {
            return "[PROGRESS] Good progress! Focus on edge cases and retry missed questions.";
        } else if (mastery.getCurrentAccuracy() >= 50) {
            return "[REVIEW] Review study materials and practice missed questions before advancing.";
        } else {
            return "[START] Start with Easy difficulty and review fundamental concepts.";
        }
    }

    private int calculateLevel(double accuracy, int totalAttempted) {
        if (totalAttempted < 5) return 1;
        if (accuracy >= 90 && totalAttempted >= 30) return 5;
        if (accuracy >= 80 && totalAttempted >= 20) return 4;
        if (accuracy >= 65 && totalAttempted >= 10) return 3;
        if (accuracy >= 50 && totalAttempted >= 5) return 2;
        return 1;
    }

    private MasteryLevel mapMastery(ResultSet rs) throws SQLException {
        MasteryLevel m = new MasteryLevel();
        m.setId(rs.getInt("id"));
        m.setUserId(rs.getInt("user_id"));
        m.setCategory(rs.getString("category"));
        m.setLevel(rs.getInt("level"));
        m.setTotalAttempted(rs.getInt("total_attempted"));
        m.setTotalCorrect(rs.getInt("total_correct"));
        m.setCurrentAccuracy(rs.getDouble("current_accuracy"));
        m.setAdvanceRecommended(rs.getInt("advance_recommended") == 1);
        return m;
    }
}
