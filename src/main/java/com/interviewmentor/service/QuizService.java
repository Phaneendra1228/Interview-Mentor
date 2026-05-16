package com.interviewmentor.service;

import com.interviewmentor.model.QuizResult;
import com.interviewmentor.model.QuizSession;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages quiz session lifecycle: creation, answer recording, and completion.
 */
public class QuizService {

    private final DatabaseService db = DatabaseService.getInstance();

    /**
     * Create a new quiz session and return its ID.
     */
    public int createSession(int userId, String category, String difficulty, int totalQuestions) {
        String sql = "INSERT INTO quiz_sessions (user_id, category, difficulty, total_questions) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, difficulty);
            ps.setInt(4, totalQuestions);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[QuizService] Create session error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Record a single question result.
     */
    public void recordAnswer(int sessionId, int questionId, String userAnswer, boolean correct, int timeSeconds) {
        String sql = "INSERT INTO quiz_results (session_id, question_id, user_answer, is_correct, time_taken_seconds) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, questionId);
            ps.setString(3, userAnswer);
            ps.setInt(4, correct ? 1 : 0);
            ps.setInt(5, timeSeconds);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[QuizService] Record answer error: " + e.getMessage());
        }
    }

    /**
     * Finalize the session with total correct and time.
     */
    public void completeSession(int sessionId, int correctAnswers, int totalTimeSeconds) {
        String sql = "UPDATE quiz_sessions SET correct_answers = ?, time_taken_seconds = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, correctAnswers);
            ps.setInt(2, totalTimeSeconds);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[QuizService] Complete session error: " + e.getMessage());
        }
    }

    /**
     * Get all sessions for a user ordered by most recent first.
     */
    public List<QuizSession> getSessionsByUser(int userId) {
        String sql = "SELECT * FROM quiz_sessions WHERE user_id = ? ORDER BY started_at DESC";
        List<QuizSession> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get recent N sessions for a user.
     */
    public List<QuizSession> getRecentSessions(int userId, int limit) {
        String sql = "SELECT * FROM quiz_sessions WHERE user_id = ? ORDER BY started_at DESC LIMIT ?";
        List<QuizSession> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get all individual results for a session.
     */
    public List<QuizResult> getResultsBySession(int sessionId) {
        String sql = "SELECT * FROM quiz_results WHERE session_id = ? ORDER BY id ASC";
        List<QuizResult> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuizResult r = new QuizResult();
                r.setId(rs.getInt("id"));
                r.setSessionId(rs.getInt("session_id"));
                r.setQuestionId(rs.getInt("question_id"));
                r.setUserAnswer(rs.getString("user_answer"));
                r.setCorrect(rs.getInt("is_correct") == 1);
                r.setTimeTakenSeconds(rs.getInt("time_taken_seconds"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] Error: " + e.getMessage());
        }
        return list;
    }

    private QuizSession mapSession(ResultSet rs) throws SQLException {
        QuizSession s = new QuizSession();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setCategory(rs.getString("category"));
        s.setDifficulty(rs.getString("difficulty"));
        s.setTotalQuestions(rs.getInt("total_questions"));
        s.setCorrectAnswers(rs.getInt("correct_answers"));
        s.setTimeTakenSeconds(rs.getInt("time_taken_seconds"));
        String startedStr = rs.getString("started_at");
        if (startedStr != null) {
            try {
                s.setStartedAt(LocalDateTime.parse(startedStr.replace(" ", "T")));
            } catch (Exception e) {
                s.setStartedAt(LocalDateTime.now());
            }
        }
        return s;
    }
}
