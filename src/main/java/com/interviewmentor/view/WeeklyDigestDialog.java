package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.model.User;
import com.interviewmentor.service.AnalyticsService;
import com.interviewmentor.service.DatabaseService;
import com.interviewmentor.service.StreakService;
import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Weekly Progress Digest — auto-generated 7-day performance summary.
 */
public class WeeklyDigestDialog extends JDialog {

    private final MainFrame mainFrame;
    private final StreakService streakService = new StreakService();
    private final DatabaseService db = DatabaseService.getInstance();

    public WeeklyDigestDialog(MainFrame mainFrame) {
        super(mainFrame, "Weekly Progress Digest", true);
        this.mainFrame = mainFrame;
        setSize(520, 620);
        setLocationRelativeTo(mainFrame);
        setResizable(false);
        getContentPane().setBackground(Constants.BG_PRIMARY);
        buildUI();
    }

    private void buildUI() {
        User user = mainFrame.getCurrentUser();
        if (user == null) return;
        int userId = user.getId();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JLabel title = new JLabel("📊 Your Weekly Digest");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        JLabel period = new JLabel(startDate.format(DateTimeFormatter.ofPattern("MMM d")) + " — " +
                endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        period.setFont(Constants.FONT_BODY);
        period.setForeground(Constants.TEXT_MUTED);
        period.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(period);
        content.add(Box.createVerticalStrut(20));

        // Fetch weekly stats
        int weekQuizzes = getWeekCount(userId, "SELECT COUNT(*) FROM quiz_sessions WHERE user_id = ? AND started_at >= datetime('now', '-7 days')");
        int weekQuestions = getWeekCount(userId, "SELECT COALESCE(SUM(total_questions), 0) FROM quiz_sessions WHERE user_id = ? AND started_at >= datetime('now', '-7 days')");
        int weekCorrect = getWeekCount(userId, "SELECT COALESCE(SUM(correct_answers), 0) FROM quiz_sessions WHERE user_id = ? AND started_at >= datetime('now', '-7 days')");
        int weekTime = getWeekCount(userId, "SELECT COALESCE(SUM(time_taken_seconds), 0) FROM quiz_sessions WHERE user_id = ? AND started_at >= datetime('now', '-7 days')");
        double weekAccuracy = weekQuestions > 0 ? (weekCorrect * 100.0 / weekQuestions) : 0;
        int streak = streakService.getCurrentStreak(userId);

        // Stat cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createDigestStatCard("Quizzes", String.valueOf(weekQuizzes), Constants.ACCENT_PRIMARY));
        statsRow.add(createDigestStatCard("Questions", String.valueOf(weekQuestions), Constants.ACCENT_SECONDARY));
        statsRow.add(createDigestStatCard("Accuracy", String.format("%.1f%%", weekAccuracy), 
                weekAccuracy >= 70 ? Constants.ACCENT_SUCCESS : Constants.ACCENT_WARNING));
        content.add(statsRow);
        content.add(Box.createVerticalStrut(16));

        JPanel statsRow2 = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow2.setOpaque(false);
        statsRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        statsRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow2.add(createDigestStatCard("Study Time", formatTime(weekTime), Constants.ACCENT_INFO));
        statsRow2.add(createDigestStatCard("Correct", String.valueOf(weekCorrect), Constants.ACCENT_SUCCESS));
        statsRow2.add(createDigestStatCard("Streak", streak + " days", Constants.ACCENT_ORANGE));
        content.add(statsRow2);
        content.add(Box.createVerticalStrut(20));

        // Performance grade
        String grade;
        Color gradeColor;
        String message;
        if (weekQuizzes == 0) {
            grade = "—";
            gradeColor = Constants.TEXT_MUTED;
            message = "No activity this week. Start a quiz to get your weekly grade!";
        } else if (weekAccuracy >= 85 && weekQuizzes >= 5) {
            grade = "A+";
            gradeColor = Constants.ACCENT_SUCCESS;
            message = "Outstanding week! You're well on track for interview success.";
        } else if (weekAccuracy >= 70 && weekQuizzes >= 3) {
            grade = "A";
            gradeColor = Constants.ACCENT_SUCCESS;
            message = "Great progress! Keep up the consistent practice.";
        } else if (weekAccuracy >= 60 && weekQuizzes >= 2) {
            grade = "B";
            gradeColor = Constants.ACCENT_INFO;
            message = "Good effort! Try to increase quiz frequency for best results.";
        } else if (weekAccuracy >= 50) {
            grade = "C";
            gradeColor = Constants.ACCENT_WARNING;
            message = "Room for improvement. Focus on your weak areas.";
        } else {
            grade = "D";
            gradeColor = Constants.ACCENT_DANGER;
            message = "Keep pushing! Review fundamentals and practice daily.";
        }

        JPanel gradeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(gradeColor, 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Constants.withAlpha(gradeColor, 50));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        gradeCard.setOpaque(false);
        gradeCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 16));
        gradeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        gradeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel gradeLabel = new JLabel(grade);
        gradeLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 36));
        gradeLabel.setForeground(gradeColor);
        gradeCard.add(gradeLabel);

        JPanel gradeInfo = new JPanel();
        gradeInfo.setOpaque(false);
        gradeInfo.setLayout(new BoxLayout(gradeInfo, BoxLayout.Y_AXIS));
        JLabel gradeTitle = new JLabel("Weekly Performance Grade");
        gradeTitle.setFont(Constants.FONT_BODY_BOLD);
        gradeTitle.setForeground(Constants.TEXT_PRIMARY);
        gradeInfo.add(gradeTitle);
        JLabel gradeMsg = new JLabel(message);
        gradeMsg.setFont(Constants.FONT_SMALL);
        gradeMsg.setForeground(Constants.TEXT_SECONDARY);
        gradeInfo.add(gradeMsg);
        gradeCard.add(gradeInfo);

        content.add(gradeCard);
        content.add(Box.createVerticalStrut(20));

        // Best & worst categories this week
        String bestCat = getWeekBestCategory(userId, true);
        String worstCat = getWeekBestCategory(userId, false);

        if (bestCat != null || worstCat != null) {
            JPanel catRow = new JPanel(new GridLayout(1, 2, 12, 0));
            catRow.setOpaque(false);
            catRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            catRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (bestCat != null) {
                catRow.add(createCatHighlight("🏆 Strongest", bestCat, Constants.ACCENT_SUCCESS));
            }
            if (worstCat != null) {
                catRow.add(createCatHighlight("⚠ Needs Work", worstCat, Constants.ACCENT_WARNING));
            }
            content.add(catRow);
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        setContentPane(scroll);
    }

    private JPanel createDigestStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 18));
        valLbl.setForeground(Constants.TEXT_PRIMARY);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(Constants.FONT_SMALL);
        lblLbl.setForeground(Constants.TEXT_MUTED);
        lblLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblLbl);
        return card;
    }

    private JPanel createCatHighlight(String header, String catName, Color color) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel hdr = new JLabel(header);
        hdr.setFont(Constants.FONT_SMALL);
        hdr.setForeground(color);
        hdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hdr);
        card.add(Box.createVerticalStrut(4));
        JLabel name = new JLabel(catName);
        name.setFont(Constants.FONT_BODY_BOLD);
        name.setForeground(Constants.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(name);
        return card;
    }

    private int getWeekCount(int userId, String sql) {
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getWeekBestCategory(int userId, boolean best) {
        String sql = """
            SELECT category,
                   CASE WHEN SUM(total_questions) > 0
                        THEN SUM(correct_answers) * 100.0 / SUM(total_questions) ELSE 0 END AS accuracy
            FROM quiz_sessions
            WHERE user_id = ? AND started_at >= datetime('now', '-7 days') AND total_questions > 0
            GROUP BY category
            ORDER BY accuracy """ + (best ? "DESC" : "ASC") + " LIMIT 1";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Category cat = Category.fromString(rs.getString("category"));
                return cat != null ? cat.getDisplayName() : rs.getString("category");
            }
        } catch (Exception e) { /* ignored */ }
        return null;
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 60) return totalSeconds + "s";
        if (totalSeconds < 3600) return (totalSeconds / 60) + "m";
        return String.format("%dh %dm", totalSeconds / 3600, (totalSeconds % 3600) / 60);
    }
}
