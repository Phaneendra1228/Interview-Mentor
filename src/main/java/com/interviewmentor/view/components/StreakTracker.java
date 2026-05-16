package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Visual 7-day activity heatmap component showing which days the user has practiced.
 * Each day is rendered as a colored square based on quiz activity intensity.
 */
public class StreakTracker extends JPanel {

    private int[] dailyActivity = new int[7]; // quizzes per day, index 0 = 6 days ago
    private int currentStreak = 0;
    private int longestStreak = 0;
    private float animProgress = 0f;
    private Timer animTimer;

    private static final String[] DAY_LABELS = {"M", "T", "W", "T", "F", "S", "S"};

    public StreakTracker() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 110));
    }

    public void setData(int[] activity, int currentStreak, int longestStreak) {
        this.dailyActivity = activity != null && activity.length == 7 ? activity : new int[7];
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        animate();
    }

    private void animate() {
        animProgress = 0f;
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(16, e -> {
            animProgress += 0.06f;
            if (animProgress >= 1f) {
                animProgress = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background card
        g2.setColor(Constants.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

        int padding = 16;

        // Title + streak badge
        g2.setColor(Constants.TEXT_PRIMARY);
        g2.setFont(Constants.FONT_BODY_BOLD);
        g2.drawString("Weekly Activity", padding, padding + 14);

        // Fire icon + streak count
        String streakText = currentStreak + " day streak";
        g2.setColor(Constants.ACCENT_ORANGE);
        g2.setFont(Constants.FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(streakText);
        int textX = w - padding - textWidth;
        g2.drawString(streakText, textX, padding + 14);

        // Draw custom flame (teardrop) shape
        int fw = 10;
        int fh = 14;
        int fx = textX - fw - 6;
        int fy = padding + 2;
        
        java.awt.geom.Path2D.Float flame = new java.awt.geom.Path2D.Float();
        flame.moveTo(fx + 5, fy + fh);
        flame.curveTo(fx + 5, fy + 14, fx + 0, fy + 10, fx + 0, fy + 7);
        flame.curveTo(fx + 0, fy + 3, fx + 5, fy + 0, fx + 5, fy + 0);
        flame.curveTo(fx + 5, fy + 0, fx + 3, fy + 2, fx + 3, fy + 5);
        flame.curveTo(fx + 3, fy + 8, fx + 7, fy + 8, fx + 7, fy + 8);
        flame.curveTo(fx + 7, fy + 8, fx + 7, fy + 5, fx + 9, fy + 3);
        flame.curveTo(fx + 9, fy + 3, fx + 10, fy + 5, fx + 10, fy + 7);
        flame.curveTo(fx + 10, fy + 10, fx + 5, fy + 14, fx + 5, fy + 14);
        flame.closePath();
        g2.fill(flame);

        // Heatmap squares
        int squareSize = 32;
        int gap = 8;
        int totalWidth = 7 * squareSize + 6 * gap;
        int startX = (w - totalWidth) / 2;
        int startY = padding + 30;

        // Determine today's day index (0=Mon, 6=Sun)
        int todayDow = java.time.LocalDate.now().getDayOfWeek().getValue() - 1; // 0-indexed Mon=0

        for (int i = 0; i < 7; i++) {
            int x = startX + i * (squareSize + gap);
            int y = startY;

            // Calculate which day label to show
            int dayIndex = (todayDow - 6 + i + 7) % 7;

            // Activity intensity color
            float progress = Math.min(animProgress * 1.5f, 1f);
            int activity = (int)(dailyActivity[i] * progress);
            Color squareColor;
            if (activity == 0) squareColor = Constants.STREAK_NONE;
            else if (activity <= 1) squareColor = Constants.STREAK_LOW;
            else if (activity <= 3) squareColor = Constants.STREAK_MED;
            else if (activity <= 5) squareColor = Constants.STREAK_HIGH;
            else squareColor = Constants.STREAK_MAX;

            // Draw square with animation scale
            float scale = Math.min(1f, animProgress * 2f - i * 0.1f);
            scale = Math.max(0, scale);

            int scaledSize = (int)(squareSize * scale);
            int offset = (squareSize - scaledSize) / 2;

            g2.setColor(squareColor);
            g2.fill(new RoundRectangle2D.Float(x + offset, y + offset, scaledSize, scaledSize, 8, 8));

            // Glow effect for active days
            if (activity > 0 && scale >= 1f) {
                g2.setColor(Constants.withAlpha(squareColor, 40));
                g2.fill(new RoundRectangle2D.Float(x - 2, y - 2, squareSize + 4, squareSize + 4, 10, 10));
                g2.setColor(squareColor);
                g2.fill(new RoundRectangle2D.Float(x, y, squareSize, squareSize, 8, 8));
            }

            // Today highlight ring
            if (i == 6) {
                g2.setColor(Constants.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(x - 1, y - 1, squareSize + 2, squareSize + 2, 9, 9));
            }

            // Draw date number inside the square
            if (scale >= 0.5f) {
                java.time.LocalDate squareDate = java.time.LocalDate.now().minusDays(6 - i);
                String dateStr = String.valueOf(squareDate.getDayOfMonth());
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 12));
                fm = g2.getFontMetrics();
                int textW = fm.stringWidth(dateStr);
                int textH = fm.getAscent();
                
                if (activity == 0) {
                    g2.setColor(Constants.TEXT_MUTED);
                } else {
                    g2.setColor(new Color(255, 255, 255, 220)); // Bright text for active days
                }
                
                g2.drawString(dateStr, x + (squareSize - textW) / 2, y + (squareSize + textH) / 2 - 1);
            }

            // Day label below square
            g2.setColor(Constants.TEXT_MUTED);
            g2.setFont(Constants.FONT_TINY);
            fm = g2.getFontMetrics();
            String label = DAY_LABELS[dayIndex];
            int labelX = x + (squareSize - fm.stringWidth(label)) / 2;
            g2.drawString(label, labelX, y + squareSize + 14);
        }

        // Longest streak info
        g2.setColor(Constants.TEXT_MUTED);
        g2.setFont(Constants.FONT_TINY);
        g2.drawString("Longest: " + longestStreak + " days", padding, h - 6);

        g2.dispose();
    }
}
