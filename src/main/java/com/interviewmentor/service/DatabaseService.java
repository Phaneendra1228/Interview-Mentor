package com.interviewmentor.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.interviewmentor.model.Question;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

/**
 * Singleton service for SQLite database lifecycle:
 * connection management, schema creation, and question seeding.
 */
public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:interview_mentor.db";
    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {}

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /** Initialize DB: create tables and seed questions if needed. */
    public void initialize() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createTables();
            seedQuestionsIfEmpty();
            System.out.println("[DB] Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                target_role TEXT,
                experience_level TEXT,
                tech_stack TEXT
            )
        """);
        
        try {
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN target_role TEXT");
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN experience_level TEXT");
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN tech_stack TEXT");
        } catch (SQLException ignored) {
        }

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                question_text TEXT NOT NULL,
                option_a TEXT,
                option_b TEXT,
                option_c TEXT,
                option_d TEXT,
                correct_answer TEXT NOT NULL,
                explanation TEXT,
                tags TEXT
            )
        """);

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS quiz_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                difficulty TEXT,
                total_questions INTEGER DEFAULT 0,
                correct_answers INTEGER DEFAULT 0,
                time_taken_seconds INTEGER DEFAULT 0,
                started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS quiz_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                user_answer TEXT,
                is_correct INTEGER DEFAULT 0,
                time_taken_seconds INTEGER DEFAULT 0,
                FOREIGN KEY (session_id) REFERENCES quiz_sessions(id),
                FOREIGN KEY (question_id) REFERENCES questions(id)
            )
        """);

        // New tables for bookmarks and daily activity
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (question_id) REFERENCES questions(id),
                UNIQUE(user_id, question_id)
            )
        """);
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS star_stories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                question TEXT NOT NULL,
                situation TEXT,
                task TEXT,
                action TEXT,
                result TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS daily_activity (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                activity_date TEXT NOT NULL,
                quiz_count INTEGER DEFAULT 0,
                questions_answered INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, activity_date)
            )
        """);

        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS achievements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                achievement_id TEXT NOT NULL,
                earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, achievement_id)
            )
        """);

        // Training plans: goals, mock parameters, action plans
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS training_plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                goal TEXT,
                difficulty TEXT,
                target_score INTEGER DEFAULT 70,
                achieved_score INTEGER DEFAULT 0,
                goal_met INTEGER DEFAULT 0,
                action_plan TEXT,
                status TEXT DEFAULT 'ACTIVE',
                is_mock INTEGER DEFAULT 0,
                mock_time_limit INTEGER DEFAULT 0,
                mock_format TEXT DEFAULT 'RELAXED',
                real_time_feedback INTEGER DEFAULT 0,
                voice_capture INTEGER DEFAULT 0,
                extras_enabled INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                completed_at TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        // Session logs: voice/text response logs per question
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS session_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                log_type TEXT DEFAULT 'TEXT',
                content TEXT,
                response_data TEXT,
                confidence_score REAL DEFAULT 0.0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (session_id) REFERENCES quiz_sessions(id),
                FOREIGN KEY (question_id) REFERENCES questions(id)
            )
        """);

        // Mastery tracking: per-category mastery levels
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS mastery_tracking (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                level INTEGER DEFAULT 1,
                total_attempted INTEGER DEFAULT 0,
                total_correct INTEGER DEFAULT 0,
                current_accuracy REAL DEFAULT 0.0,
                advance_recommended INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, category)
            )
        """);

        // Error logs: issue tracking and retry
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS error_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                session_id INTEGER,
                error_type TEXT,
                error_message TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);

        // Spaced repetition: SM-2 algorithm tracking
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS spaced_repetition (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                ease_factor REAL DEFAULT 2.5,
                interval_days INTEGER DEFAULT 0,
                repetitions INTEGER DEFAULT 0,
                next_review TEXT NOT NULL,
                last_reviewed TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (question_id) REFERENCES questions(id),
                UNIQUE(user_id, question_id)
            )
        """);

        stmt.close();
    }

    private void seedQuestionsIfEmpty() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions");
            int count = rs.getInt(1);
            rs.close();
            stmt.close();

            if (count == 0) {
                System.out.println("[DB] Seeding question bank...");
                String[] jsonFiles = {
                    "data_structures.json", "algorithms.json",
                    "operating_systems.json", "dbms.json",
                    "java_programming.json", "python_programming.json",
                    "oop_concepts.json", "computer_networks.json",
                    "behavioral.json"
                };
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Question>>() {}.getType();

                for (String file : jsonFiles) {
                    InputStream is = getClass().getResourceAsStream("/questions/" + file);
                    if (is == null) {
                        System.err.println("[DB] Question file not found: " + file);
                        continue;
                    }
                    List<Question> questions = gson.fromJson(
                        new InputStreamReader(is, StandardCharsets.UTF_8), listType
                    );
                    insertQuestions(questions);
                    System.out.println("[DB] Loaded " + questions.size() + " questions from " + file);
                }
            } else {
                System.out.println("[DB] Question bank already seeded (" + count + " questions).");
            }
        } catch (Exception e) {
            System.err.println("[DB] Seeding error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertQuestions(List<Question> questions) throws SQLException {
        String sql = """
            INSERT INTO questions (category, difficulty, question_text,
                option_a, option_b, option_c, option_d,
                correct_answer, explanation, tags)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = connection.prepareStatement(sql);
        for (Question q : questions) {
            ps.setString(1, q.getCategory());
            ps.setString(2, q.getDifficulty());
            ps.setString(3, q.getQuestionText());
            ps.setString(4, q.getOptionA());
            ps.setString(5, q.getOptionB());
            ps.setString(6, q.getOptionC());
            ps.setString(7, q.getOptionD());
            ps.setString(8, q.getCorrectAnswer());
            ps.setString(9, q.getExplanation());
            ps.setString(10, q.getTags());
            ps.addBatch();
        }
        ps.executeBatch();
        ps.close();
    }
}
