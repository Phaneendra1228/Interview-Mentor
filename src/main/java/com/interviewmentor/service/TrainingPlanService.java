package com.interviewmentor.service;

import com.interviewmentor.model.TrainingPlan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages training plans, goals, and action plans.
 * Maps to flowchart: "Set Goal / Fetch Session", "Finalize Plan",
 * "Fetch Mock Parameters", "Goal Success?", "Training Action Plan",
 * "Log Issue & Retransmit".
 */
public class TrainingPlanService {

    private final DatabaseService db = DatabaseService.getInstance();

    /** Create a new training plan and return its ID. */
    public int createPlan(TrainingPlan plan) {
        String sql = """
            INSERT INTO training_plans (user_id, category, goal, difficulty,
                target_score, status, is_mock, mock_time_limit, mock_format,
                real_time_feedback, voice_capture, extras_enabled)
            VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, plan.getUserId());
            ps.setString(2, plan.getCategory());
            ps.setString(3, plan.getGoal());
            ps.setString(4, plan.getDifficulty());
            ps.setInt(5, plan.getTargetScore());
            ps.setInt(6, plan.isMockInterview() ? 1 : 0);
            ps.setInt(7, plan.getMockTimeLimitSeconds());
            ps.setString(8, plan.getMockFormat());
            ps.setInt(9, plan.isRealTimeFeedback() ? 1 : 0);
            ps.setInt(10, plan.isVoiceCaptureEnabled() ? 1 : 0);
            ps.setInt(11, plan.isExtrasEnabled() ? 1 : 0);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Create error: " + e.getMessage());
        }
        return -1;
    }

    /** Complete a training plan with results and action plan. */
    public void completePlan(int planId, int achievedScore, boolean goalMet, String actionPlan) {
        String sql = """
            UPDATE training_plans SET achieved_score = ?, goal_met = ?,
                action_plan = ?, status = ?, completed_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, achievedScore);
            ps.setInt(2, goalMet ? 1 : 0);
            ps.setString(3, actionPlan);
            ps.setString(4, goalMet ? "COMPLETED" : "NEEDS_RETRY");
            ps.setInt(5, planId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Complete error: " + e.getMessage());
        }
    }

    /** Get active training plans for a user. */
    public List<TrainingPlan> getActivePlans(int userId) {
        return getPlansByStatus(userId, "ACTIVE");
    }

    /** Get all training plans for a user. */
    public List<TrainingPlan> getAllPlans(int userId) {
        String sql = "SELECT * FROM training_plans WHERE user_id = ? ORDER BY created_at DESC";
        return queryPlans(sql, userId);
    }

    /** Get recent N plans. */
    public List<TrainingPlan> getRecentPlans(int userId, int limit) {
        String sql = "SELECT * FROM training_plans WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<TrainingPlan> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPlan(rs));
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Query error: " + e.getMessage());
        }
        return list;
    }

    /** Get plans needing retry (failed goals). */
    public List<TrainingPlan> getRetryPlans(int userId) {
        return getPlansByStatus(userId, "NEEDS_RETRY");
    }

    private List<TrainingPlan> getPlansByStatus(int userId, String status) {
        String sql = "SELECT * FROM training_plans WHERE user_id = ? AND status = ? ORDER BY created_at DESC";
        List<TrainingPlan> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPlan(rs));
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Query error: " + e.getMessage());
        }
        return list;
    }

    private List<TrainingPlan> queryPlans(String sql, int userId) {
        List<TrainingPlan> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPlan(rs));
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Query error: " + e.getMessage());
        }
        return list;
    }

    /** Get plan by ID. */
    public TrainingPlan getPlanById(int planId) {
        String sql = "SELECT * FROM training_plans WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapPlan(rs);
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Query error: " + e.getMessage());
        }
        return null;
    }

    /** Log an error/issue (flowchart: "Log Issue & Retransmit"). */
    public void logError(int userId, int sessionId, String errorType, String errorMessage) {
        String sql = "INSERT INTO error_logs (user_id, session_id, error_type, error_message) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            ps.setString(3, errorType);
            ps.setString(4, errorMessage);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TrainingPlanService] Log error: " + e.getMessage());
        }
    }

    /** Generate an action plan based on quiz performance. */
    public String generateActionPlan(String category, double accuracy, int totalQuestions,
                                      int missedCount, String difficulty) {
        StringBuilder plan = new StringBuilder();
        plan.append("=== Training Action Plan ===\n\n");

        if (accuracy >= 90) {
            plan.append("★ EXCELLENT PERFORMANCE\n");
            plan.append("• You've demonstrated strong mastery in ").append(category).append(".\n");
            plan.append("• Consider advancing to ").append("Hard".equals(difficulty) ? "mock interview scenarios" : "harder difficulty").append(".\n");
            plan.append("• Focus on speed optimization — try timed practice.\n");
        } else if (accuracy >= 70) {
            plan.append("✓ GOOD PERFORMANCE — Minor Gaps\n");
            plan.append("• Review the ").append(missedCount).append(" incorrect answer(s) carefully.\n");
            plan.append("• Focus on edge cases and tricky concepts in ").append(category).append(".\n");
            plan.append("• Recommended: Retry missed questions, then attempt a harder session.\n");
        } else if (accuracy >= 50) {
            plan.append("△ IMPROVEMENT POSSIBLE\n");
            plan.append("• Significant knowledge gaps detected in ").append(category).append(".\n");
            plan.append("• Step 1: Review study materials in the Resources section.\n");
            plan.append("• Step 2: Practice missed questions (").append(missedCount).append(" missed).\n");
            plan.append("• Step 3: Retake this category at the same difficulty.\n");
        } else {
            plan.append("▲ NEEDS SIGNIFICANT WORK\n");
            plan.append("• Fundamental concepts need strengthening in ").append(category).append(".\n");
            plan.append("• Step 1: Start with Easy difficulty to build confidence.\n");
            plan.append("• Step 2: Watch recommended YouTube videos in Resources.\n");
            plan.append("• Step 3: Read PDF study notes for ").append(category).append(".\n");
            plan.append("• Step 4: Retry with smaller question sets (5 questions).\n");
        }

        plan.append("\n• Total questions attempted: ").append(totalQuestions);
        plan.append("\n• Questions missed: ").append(missedCount);
        plan.append("\n• Achieved accuracy: ").append(String.format("%.1f%%", accuracy));
        return plan.toString();
    }

    private TrainingPlan mapPlan(ResultSet rs) throws SQLException {
        TrainingPlan p = new TrainingPlan();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setCategory(rs.getString("category"));
        p.setGoal(rs.getString("goal"));
        p.setDifficulty(rs.getString("difficulty"));
        p.setTargetScore(rs.getInt("target_score"));
        p.setAchievedScore(rs.getInt("achieved_score"));
        p.setGoalMet(rs.getInt("goal_met") == 1);
        p.setActionPlan(rs.getString("action_plan"));
        p.setStatus(rs.getString("status"));
        p.setMockInterview(rs.getInt("is_mock") == 1);
        p.setMockTimeLimitSeconds(rs.getInt("mock_time_limit"));
        p.setMockFormat(rs.getString("mock_format"));
        p.setRealTimeFeedback(rs.getInt("real_time_feedback") == 1);
        p.setVoiceCaptureEnabled(rs.getInt("voice_capture") == 1);
        p.setExtrasEnabled(rs.getInt("extras_enabled") == 1);
        String created = rs.getString("created_at");
        if (created != null) {
            try { p.setCreatedAt(LocalDateTime.parse(created.replace(" ", "T"))); }
            catch (Exception e) { p.setCreatedAt(LocalDateTime.now()); }
        }
        String completed = rs.getString("completed_at");
        if (completed != null) {
            try { p.setCompletedAt(LocalDateTime.parse(completed.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return p;
    }
}
