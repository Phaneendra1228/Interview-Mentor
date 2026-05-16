package com.interviewmentor.service;

import com.interviewmentor.model.Question;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SM-2 Spaced Repetition Algorithm service.
 * Optimizes review scheduling based on user performance per question.
 */
public class SpacedRepetitionService {

    private final DatabaseService db = DatabaseService.getInstance();
    private final QuestionService questionService = new QuestionService();

    /** Record a review result and update the SM-2 schedule. quality: 0-5 */
    public void recordReview(int userId, int questionId, int quality) {
        quality = Math.max(0, Math.min(5, quality));
        String sel = "SELECT * FROM spaced_repetition WHERE user_id = ? AND question_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sel)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ResultSet rs = ps.executeQuery();
            double ef; int reps, interval; boolean exists = rs.next();
            if (exists) { ef = rs.getDouble("ease_factor"); reps = rs.getInt("repetitions"); interval = rs.getInt("interval_days"); }
            else { ef = 2.5; reps = 0; interval = 0; }

            if (quality >= 3) {
                if (reps == 0) interval = 1; else if (reps == 1) interval = 6;
                else interval = (int) Math.round(interval * ef);
                reps++;
            } else { reps = 0; interval = 1; }
            ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (ef < 1.3) ef = 1.3;
            String nextReview = LocalDate.now().plusDays(interval).toString();
            String today = LocalDate.now().toString();

            if (exists) {
                try (PreparedStatement u = db.getConnection().prepareStatement(
                    "UPDATE spaced_repetition SET ease_factor=?, interval_days=?, repetitions=?, next_review=?, last_reviewed=? WHERE user_id=? AND question_id=?")) {
                    u.setDouble(1, ef); u.setInt(2, interval); u.setInt(3, reps);
                    u.setString(4, nextReview); u.setString(5, today); u.setInt(6, userId); u.setInt(7, questionId);
                    u.executeUpdate();
                }
            } else {
                try (PreparedStatement i = db.getConnection().prepareStatement(
                    "INSERT INTO spaced_repetition (user_id,question_id,ease_factor,interval_days,repetitions,next_review,last_reviewed) VALUES(?,?,?,?,?,?,?)")) {
                    i.setInt(1, userId); i.setInt(2, questionId); i.setDouble(3, ef);
                    i.setInt(4, interval); i.setInt(5, reps); i.setString(6, nextReview); i.setString(7, today);
                    i.executeUpdate();
                }
            }
        } catch (SQLException e) { System.err.println("[SR] Error: " + e.getMessage()); }
    }

    /** Simplified: correct=quality 4, incorrect=quality 1 */
    public void recordReview(int userId, int questionId, boolean correct) {
        recordReview(userId, questionId, correct ? 4 : 1);
    }

    /** Get questions due for review today. */
    public List<Question> getDueQuestions(int userId, int limit) {
        String today = LocalDate.now().toString();
        String sql = "SELECT question_id FROM spaced_repetition WHERE user_id=? AND next_review<=? ORDER BY next_review ASC, ease_factor ASC LIMIT ?";
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setString(2, today); ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { Question q = questionService.getQuestionById(rs.getInt("question_id")); if (q != null) questions.add(q); }
        } catch (SQLException e) { System.err.println("[SR] Due query error: " + e.getMessage()); }
        return questions;
    }

    /** Count of questions due today. */
    public int getDueCount(int userId) {
        String today = LocalDate.now().toString();
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) FROM spaced_repetition WHERE user_id=? AND next_review<=?")) {
            ps.setInt(1, userId); ps.setString(2, today); return ps.executeQuery().getInt(1);
        } catch (SQLException e) { return 0; }
    }

    /** Total tracked questions. */
    public int getTotalTracked(int userId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) FROM spaced_repetition WHERE user_id=?")) {
            ps.setInt(1, userId); return ps.executeQuery().getInt(1);
        } catch (SQLException e) { return 0; }
    }

    /** Questions mastered (interval >= 21 days). */
    public int getMasteredCount(int userId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) FROM spaced_repetition WHERE user_id=? AND interval_days>=21 AND ease_factor>=2.0")) {
            ps.setInt(1, userId); return ps.executeQuery().getInt(1);
        } catch (SQLException e) { return 0; }
    }

    /** Add a question to the SR system. */
    public void addQuestion(int userId, int questionId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT COUNT(*) FROM spaced_repetition WHERE user_id=? AND question_id=?")) {
            ps.setInt(1, userId); ps.setInt(2, questionId);
            if (ps.executeQuery().getInt(1) > 0) return;
        } catch (SQLException e) { return; }
        String today = LocalDate.now().toString();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
            "INSERT INTO spaced_repetition (user_id,question_id,ease_factor,interval_days,repetitions,next_review,last_reviewed) VALUES(?,?,2.5,0,0,?,?)")) {
            ps.setInt(1, userId); ps.setInt(2, questionId); ps.setString(3, today); ps.setString(4, today);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[SR] Add error: " + e.getMessage()); }
    }

    /** Add all missed questions from a session to SR queue. */
    public void addMissedFromSession(int userId, int sessionId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT question_id FROM quiz_results WHERE session_id=? AND is_correct=0")) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) addQuestion(userId, rs.getInt("question_id"));
        } catch (SQLException e) { System.err.println("[SR] Session scan error: " + e.getMessage()); }
    }
}
