package com.interviewmentor.service;

import com.interviewmentor.model.QuizResult;
import com.interviewmentor.model.QuizSession;
import com.interviewmentor.model.User;
import com.interviewmentor.util.ApiClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CloudSyncService {

    private final DatabaseService db = DatabaseService.getInstance();

    public boolean syncUserData(User user) {
        if (user == null) return false;

        try {
            // Sync User Profile
            ApiClient.post("/users", user, User.class);

            // Sync Quiz Sessions
            List<QuizSession> sessions = getSessionsToSync(user.getId());
            for (QuizSession session : sessions) {
                // Sync session
                QuizSession syncedSession = ApiClient.post("/sessions", session, QuizSession.class);
                if (syncedSession != null) {
                    // Sync results for this session
                    List<QuizResult> results = getResultsToSync(session.getId());
                    for (QuizResult result : results) {
                        ApiClient.post("/results", result, QuizResult.class);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<QuizSession> getSessionsToSync(int userId) {
        List<QuizSession> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_sessions WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuizSession s = new QuizSession();
                s.setId(rs.getInt("id"));
                s.setUserId(rs.getInt("user_id"));
                s.setCategory(rs.getString("category"));
                s.setDifficulty(rs.getString("difficulty"));
                s.setTotalQuestions(rs.getInt("total_questions"));
                s.setCorrectAnswers(rs.getInt("correct_answers"));
                s.setTimeTakenSeconds(rs.getInt("time_taken_seconds"));
                String dateStr = rs.getString("started_at");
                if (dateStr != null) s.setStartedAt(LocalDateTime.parse(dateStr.replace(" ", "T")));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<QuizResult> getResultsToSync(int sessionId) {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE session_id = ?";
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
            e.printStackTrace();
        }
        return list;
    }
}
