package com.interviewmentor.service;

import com.interviewmentor.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides question retrieval and random selection for quizzes.
 */
public class QuestionService {

    private final DatabaseService db = DatabaseService.getInstance();

    /**
     * Get random questions for a quiz session.
     * @param category  e.g. "DATA_STRUCTURES"
     * @param difficulty "Easy", "Medium", "Hard", or "Mixed" for all
     * @param count     number of questions to return
     */
    public List<Question> getRandomQuestions(String category, String difficulty, int count) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM questions WHERE category = ?"
        );
        if (!"Mixed".equalsIgnoreCase(difficulty)) {
            sql.append(" AND difficulty = ?");
        }
        sql.append(" ORDER BY RANDOM() LIMIT ?");

        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, category);
            if (!"Mixed".equalsIgnoreCase(difficulty)) {
                ps.setString(idx++, difficulty);
            }
            ps.setInt(idx, count);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapQuestion(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuestionService] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get the total number of questions for a category.
     */
    public int getQuestionCount(String category) {
        String sql = "SELECT COUNT(*) FROM questions WHERE category = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Get question count by category and difficulty.
     */
    public int getQuestionCount(String category, String difficulty) {
        if ("Mixed".equalsIgnoreCase(difficulty)) return getQuestionCount(category);
        String sql = "SELECT COUNT(*) FROM questions WHERE category = ? AND difficulty = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setString(2, difficulty);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Fetch a single question by ID.
     */
    public Question getQuestionById(int id) {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapQuestion(rs);
        } catch (SQLException e) {
            System.err.println("[QuestionService] Error: " + e.getMessage());
        }
        return null;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setCategory(rs.getString("category"));
        q.setDifficulty(rs.getString("difficulty"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        q.setExplanation(rs.getString("explanation"));
        q.setTags(rs.getString("tags"));
        return q;
    }
}
