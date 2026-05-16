package com.interviewmentor.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages bookmark operations: add, remove, check, and list bookmarked questions.
 */
public class BookmarkService {

    private final DatabaseService db = DatabaseService.getInstance();

    /** Toggle bookmark: add if not bookmarked, remove if already bookmarked. Returns new state. */
    public boolean toggleBookmark(int userId, int questionId) {
        if (isBookmarked(userId, questionId)) {
            removeBookmark(userId, questionId);
            return false;
        } else {
            addBookmark(userId, questionId);
            return true;
        }
    }

    /** Add a bookmark. */
    public void addBookmark(int userId, int questionId) {
        String sql = "INSERT OR IGNORE INTO bookmarks (user_id, question_id) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookmarkService] Add error: " + e.getMessage());
        }
    }

    /** Remove a bookmark. */
    public void removeBookmark(int userId, int questionId) {
        String sql = "DELETE FROM bookmarks WHERE user_id = ? AND question_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookmarkService] Remove error: " + e.getMessage());
        }
    }

    /** Check if a question is bookmarked. */
    public boolean isBookmarked(int userId, int questionId) {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE user_id = ? AND question_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Get all bookmarked question IDs for a user. */
    public List<Integer> getBookmarkedQuestionIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT question_id FROM bookmarks WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("question_id"));
            }
        } catch (SQLException e) {
            System.err.println("[BookmarkService] List error: " + e.getMessage());
        }
        return ids;
    }

    /** Get total bookmark count for a user. */
    public int getBookmarkCount(int userId) {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
}
