package com.interviewmentor.service;

import com.interviewmentor.model.SessionLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages session response logs (voice/text).
 * Maps to flowchart: "Voice/Text Logs", "Regit Voice Capture",
 * "Record & Process Response", "Stand & Process Response".
 */
public class SessionLogService {

    private final DatabaseService db = DatabaseService.getInstance();

    /** Log a response (voice or text). */
    public void logResponse(int sessionId, int questionId, String logType,
                            String content, String responseData, double confidence) {
        String sql = """
            INSERT INTO session_logs (session_id, question_id, log_type,
                content, response_data, confidence_score)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, questionId);
            ps.setString(3, logType);
            ps.setString(4, content);
            ps.setString(5, responseData);
            ps.setDouble(6, confidence);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SessionLogService] Log error: " + e.getMessage());
        }
    }

    /** Get all logs for a session. */
    public List<SessionLog> getLogsBySession(int sessionId) {
        String sql = "SELECT * FROM session_logs WHERE session_id = ? ORDER BY created_at ASC";
        List<SessionLog> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) {
            System.err.println("[SessionLogService] Query error: " + e.getMessage());
        }
        return list;
    }

    /** Get logs for a specific question in a session. */
    public List<SessionLog> getLogsByQuestion(int sessionId, int questionId) {
        String sql = "SELECT * FROM session_logs WHERE session_id = ? AND question_id = ? ORDER BY created_at ASC";
        List<SessionLog> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, questionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) {
            System.err.println("[SessionLogService] Query error: " + e.getMessage());
        }
        return list;
    }

    /** Get total voice logs count for a user's sessions. */
    public int getVoiceLogCount(int userId) {
        String sql = """
            SELECT COUNT(*) FROM session_logs sl
            JOIN quiz_sessions qs ON sl.session_id = qs.id
            WHERE qs.user_id = ? AND sl.log_type = 'VOICE'
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    private SessionLog mapLog(ResultSet rs) throws SQLException {
        SessionLog log = new SessionLog();
        log.setId(rs.getInt("id"));
        log.setSessionId(rs.getInt("session_id"));
        log.setQuestionId(rs.getInt("question_id"));
        log.setLogType(rs.getString("log_type"));
        log.setContent(rs.getString("content"));
        log.setResponseData(rs.getString("response_data"));
        log.setConfidenceScore(rs.getDouble("confidence_score"));
        String created = rs.getString("created_at");
        if (created != null) {
            try { log.setCreatedAt(LocalDateTime.parse(created.replace(" ", "T"))); }
            catch (Exception e) { log.setCreatedAt(LocalDateTime.now()); }
        }
        return log;
    }
}
