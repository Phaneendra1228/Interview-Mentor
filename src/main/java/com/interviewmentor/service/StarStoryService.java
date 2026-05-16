package com.interviewmentor.service;

import com.interviewmentor.model.StarStory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StarStoryService {
    private final DatabaseService db = DatabaseService.getInstance();

    public List<StarStory> getStoriesForUser(int userId) {
        List<StarStory> list = new ArrayList<>();
        String sql = "SELECT * FROM star_stories WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StarStory s = new StarStory();
                    s.setId(rs.getInt("id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setQuestion(rs.getString("question"));
                    s.setSituation(rs.getString("situation"));
                    s.setTask(rs.getString("task"));
                    s.setAction(rs.getString("action"));
                    s.setResult(rs.getString("result"));
                    list.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveStory(StarStory s) {
        if (s.getId() == 0) {
            String sql = "INSERT INTO star_stories (user_id, question, situation, task, action, result) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection c = db.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, s.getUserId());
                ps.setString(2, s.getQuestion());
                ps.setString(3, s.getSituation());
                ps.setString(4, s.getTask());
                ps.setString(5, s.getAction());
                ps.setString(6, s.getResult());
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE star_stories SET question=?, situation=?, task=?, action=?, result=? WHERE id=? AND user_id=?";
            try (Connection c = db.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, s.getQuestion());
                ps.setString(2, s.getSituation());
                ps.setString(3, s.getTask());
                ps.setString(4, s.getAction());
                ps.setString(5, s.getResult());
                ps.setInt(6, s.getId());
                ps.setInt(7, s.getUserId());
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteStory(int id, int userId) {
        String sql = "DELETE FROM star_stories WHERE id=? AND user_id=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
