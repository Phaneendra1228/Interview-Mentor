package com.interviewmentor.service;

import com.interviewmentor.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Handles user registration and login with BCrypt password hashing.
 */
public class AuthService {

    private final DatabaseService db = DatabaseService.getInstance();

    /**
     * Register a new user. Returns the User on success, null if username already exists.
     */
    public User register(String username, String email, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, hash);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                User user = new User();
                user.setId(keys.getInt(1));
                user.setUsername(username.trim());
                user.setEmail(email.trim());
                user.setPasswordHash(hash);
                user.setCreatedAt(LocalDateTime.now());
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[Auth] Registration failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Authenticate a user. Returns User on success, null on failure.
     */
    public User login(String identifier, String password) {
        String sql = "SELECT * FROM users WHERE email = ? OR username = ?";

        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, storedHash)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(storedHash);
                    String createdStr = rs.getString("created_at");
                    if (createdStr != null) {
                        user.setCreatedAt(LocalDateTime.parse(createdStr.replace(" ", "T")));
                    }
                    user.setTargetRole(rs.getString("target_role"));
                    user.setExperienceLevel(rs.getString("experience_level"));
                    user.setTechStack(rs.getString("tech_stack"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Auth] Login failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if a username already exists.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Check if an email exists.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Update a user's password.
     */
    public boolean updatePassword(String email, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, email.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Auth] Password update failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user profile information.
     */
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET target_role = ?, experience_level = ?, tech_stack = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getTargetRole());
            ps.setString(2, user.getExperienceLevel());
            ps.setString(3, user.getTechStack());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Auth] Profile update failed: " + e.getMessage());
            return false;
        }
    }
}
