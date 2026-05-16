package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.service.AnalyticsService;
import com.interviewmentor.service.StreakService;
import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Deep-dive analytics panel with rich, custom-painted visualizations:
 *  - Gradient area chart (accuracy trend over sessions)
 *  - Animated donut chart (difficulty distribution)
 *  - Horizontal progress bars (category mastery)
 *  - Summary stat cards with animated counters
 */
public class AnalyticsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AnalyticsService analytics = new AnalyticsService();
    private final StreakService streakService = new StreakService();

    public AnalyticsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    public void refresh() {
        removeAll();
        if (mainFrame.getCurrentUser() == null) return;
        int userId = mainFrame.getCurrentUser().getId();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Title
        JLabel title = new JLabel("Deep Analytics");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        JLabel subtitle = new JLabel("Comprehensive insights into your interview preparation journey");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(28));

        // ─── Summary Stat Cards ───
        PerformanceStats overall = analytics.getOverallStats(userId);
        int totalQuizzes = analytics.getTotalQuizCount(userId);
        int totalStudyTime = analytics.getTotalStudyTime(userId);
        int currentStreak = streakService.getCurrentStreak(userId);

        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryRow.add(createGlowStatCard("Total Sessions", String.valueOf(totalQuizzes), Constants.ACCENT_PRIMARY, Constants.GRADIENT_END));
        summaryRow.add(createGlowStatCard("Questions Solved", String.valueOf(overall.getTotalAttempted()), Constants.ACCENT_SECONDARY, new Color(0x06B6D4)));
        summaryRow.add(createGlowStatCard("Study Time", formatStudyTime(totalStudyTime), Constants.ACCENT_SUCCESS, new Color(0x34D399)));
        summaryRow.add(createGlowStatCard("Current Streak", currentStreak + " days", Constants.ACCENT_ORANGE, Constants.ACCENT_WARNING));
        content.add(summaryRow);
        content.add(Box.createVerticalStrut(24));

        // ─── ROW 1: Accuracy Trend + Donut Chart ───
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Gradient Area Chart
        List<double[]> history = analytics.getSessionAccuracyHistory(userId, 30);
        JPanel trendCard = createCard();
        trendCard.setLayout(new BorderLayout());
        trendCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel trendTitle = new JLabel("Accuracy Trend");
        trendTitle.setFont(Constants.FONT_SUBHEADING);
        trendTitle.setForeground(Constants.TEXT_PRIMARY);
        trendCard.add(trendTitle, BorderLayout.NORTH);
        trendCard.add(new GradientAreaChart(history), BorderLayout.CENTER);
        row1.add(trendCard);

        // Donut Chart (Difficulty Distribution)
        int[] diffDist = analytics.getDifficultyDistribution(userId);
        double[] diffAcc = analytics.getDifficultyAccuracy(userId);
        JPanel donutCard = createCard();
        donutCard.setLayout(new BorderLayout());
        donutCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel donutTitle = new JLabel("Difficulty Breakdown");
        donutTitle.setFont(Constants.FONT_SUBHEADING);
        donutTitle.setForeground(Constants.TEXT_PRIMARY);
        donutCard.add(donutTitle, BorderLayout.NORTH);
        donutCard.add(new DonutChart(diffDist, diffAcc), BorderLayout.CENTER);
        row1.add(donutCard);

        content.add(row1);
        content.add(Box.createVerticalStrut(24));

        // ─── ROW 2: Category Mastery Bars ───
        List<PerformanceStats> catStats = analytics.getCategoryStats(userId);
        JPanel masteryCard = createCard();
        masteryCard.setLayout(new BorderLayout());
        masteryCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        masteryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        masteryCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel masteryTitle = new JLabel("Category Mastery Overview");
        masteryTitle.setFont(Constants.FONT_SUBHEADING);
        masteryTitle.setForeground(Constants.TEXT_PRIMARY);
        masteryCard.add(masteryTitle, BorderLayout.NORTH);
        masteryCard.add(new CategoryMasteryChart(catStats), BorderLayout.CENTER);
        content.add(masteryCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ═══════════════════════════════════════════════════
    //  GRADIENT-GLOW STAT CARD
    // ═══════════════════════════════════════════════════
    private JPanel createGlowStatCard(String label, String value, Color c1, Color c2) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D rect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Constants.BG_CARD);
                g2.fill(rect);
                // Top gradient accent
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                // Subtle glow at top
                g2.setPaint(new GradientPaint(0, 0, Constants.withAlpha(c1, 30), 0, 50, new Color(0, 0, 0, 0)));
                g2.fill(rect);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 24));
        valLbl.setForeground(c1);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valLbl);
        card.add(Box.createVerticalStrut(6));
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(Constants.FONT_SMALL);
        lblLbl.setForeground(Constants.TEXT_MUTED);
        lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblLbl);
        return card;
    }

    // ═══════════════════════════════════════════════════
    //  GRADIENT AREA CHART (Accuracy Trend)
    // ═══════════════════════════════════════════════════
    private static class GradientAreaChart extends JPanel {
        private final List<double[]> data;
        private float animProgress = 0f;
        private final Timer animTimer;

        GradientAreaChart(List<double[]> data) {
            this.data = data;
            setOpaque(false);
            animTimer = new Timer(16, e -> {
                animProgress += 0.03f;
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

            int w = getWidth(), h = getHeight();
            int padL = 40, padR = 16, padT = 20, padB = 30;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            if (data == null || data.size() < 2) {
                g2.setColor(Constants.TEXT_MUTED);
                g2.setFont(Constants.FONT_BODY);
                g2.drawString("Take more quizzes to see trends", padL + 20, h / 2);
                g2.dispose();
                return;
            }

            // Grid lines
            g2.setColor(Constants.withAlpha(Constants.BORDER_COLOR, 80));
            g2.setStroke(new BasicStroke(0.5f));
            for (int i = 0; i <= 4; i++) {
                int y = padT + (int)(chartH * i / 4.0);
                g2.drawLine(padL, y, w - padR, y);
                g2.setColor(Constants.TEXT_MUTED);
                g2.setFont(Constants.FONT_TINY);
                g2.drawString((100 - 25 * i) + "%", 4, y + 4);
                g2.setColor(Constants.withAlpha(Constants.BORDER_COLOR, 80));
            }

            // Build path
            int n = data.size();
            int visiblePoints = Math.max(1, (int)(n * animProgress));
            GeneralPath linePath = new GeneralPath();
            GeneralPath areaPath = new GeneralPath();

            for (int i = 0; i < visiblePoints; i++) {
                double acc = data.get(i)[1];
                float x = padL + (float)(chartW * i) / (n - 1);
                float y = padT + chartH - (float)(chartH * acc / 100.0);
                if (i == 0) {
                    linePath.moveTo(x, y);
                    areaPath.moveTo(x, padT + chartH);
                    areaPath.lineTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                    areaPath.lineTo(x, y);
                }
            }

            // Close area path
            float lastX = padL + (float)(chartW * (visiblePoints - 1)) / (n - 1);
            areaPath.lineTo(lastX, padT + chartH);
            areaPath.closePath();

            // Gradient fill under line
            g2.setPaint(new GradientPaint(0, padT, Constants.withAlpha(Constants.ACCENT_PRIMARY, 80),
                    0, padT + chartH, Constants.withAlpha(Constants.ACCENT_PRIMARY, 5)));
            g2.fill(areaPath);

            // Line
            g2.setColor(Constants.ACCENT_PRIMARY_LIGHT);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linePath);

            // Data points
            for (int i = 0; i < visiblePoints; i++) {
                double acc = data.get(i)[1];
                float x = padL + (float)(chartW * i) / (n - 1);
                float y = padT + chartH - (float)(chartH * acc / 100.0);
                g2.setColor(Constants.BG_CARD);
                g2.fillOval((int)x - 5, (int)y - 5, 10, 10);
                g2.setColor(Constants.ACCENT_PRIMARY_LIGHT);
                g2.fillOval((int)x - 3, (int)y - 3, 6, 6);
            }

            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════
    //  DONUT CHART (Difficulty Distribution)
    // ═══════════════════════════════════════════════════
    private static class DonutChart extends JPanel {
        private final int[] counts;
        private final double[] accuracy;
        private final Color[] colors = {Constants.ACCENT_SUCCESS, Constants.ACCENT_WARNING, Constants.ACCENT_DANGER};
        private final String[] labels = {"Easy", "Medium", "Hard"};
        private float animProgress = 0f;

        DonutChart(int[] counts, double[] accuracy) {
            this.counts = counts;
            this.accuracy = accuracy;
            setOpaque(false);
            Timer timer = new Timer(16, e -> {
                animProgress += 0.025f;
                if (animProgress >= 1f) {
                    animProgress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int total = counts[0] + counts[1] + counts[2];

            if (total == 0) {
                g2.setColor(Constants.TEXT_MUTED);
                g2.setFont(Constants.FONT_BODY);
                g2.drawString("No data yet", w / 2 - 30, h / 2);
                g2.dispose();
                return;
            }

            int donutSize = Math.min(w - 140, h - 40);
            int cx = donutSize / 2 + 10;
            int cy = h / 2;
            int outerR = donutSize / 2;
            int innerR = (int)(outerR * 0.6);

            float startAngle = 90;
            float totalAngle = 360f * animProgress;

            for (int i = 0; i < 3; i++) {
                if (counts[i] == 0) continue;
                float sweep = (counts[i] * totalAngle) / total;
                g2.setColor(colors[i]);
                g2.fillArc(cx - outerR, cy - outerR, outerR * 2, outerR * 2,
                        (int) startAngle, (int) sweep);
                startAngle += sweep;
            }

            // Inner circle (donut hole)
            g2.setColor(Constants.BG_CARD);
            g2.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

            // Center text
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 22));
            String totalStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalStr, cx - fm.stringWidth(totalStr) / 2, cy + 2);
            g2.setFont(Constants.FONT_TINY);
            g2.setColor(Constants.TEXT_MUTED);
            fm = g2.getFontMetrics();
            g2.drawString("sessions", cx - fm.stringWidth("sessions") / 2, cy + 16);

            // Legend
            int legendX = cx + outerR + 30;
            int legendY = cy - 50;
            for (int i = 0; i < 3; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(legendX, legendY, 14, 14, 4, 4);
                g2.setColor(Constants.TEXT_PRIMARY);
                g2.setFont(Constants.FONT_BODY_BOLD);
                g2.drawString(labels[i], legendX + 20, legendY + 12);
                g2.setColor(Constants.TEXT_MUTED);
                g2.setFont(Constants.FONT_SMALL);
                g2.drawString(counts[i] + " sessions • " + String.format("%.0f%%", accuracy[i]) + " acc", legendX + 20, legendY + 28);
                legendY += 40;
            }

            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════
    //  CATEGORY MASTERY CHART (Horizontal Bars)
    // ═══════════════════════════════════════════════════
    private static class CategoryMasteryChart extends JPanel {
        private final List<PerformanceStats> stats;
        private float animProgress = 0f;

        CategoryMasteryChart(List<PerformanceStats> stats) {
            this.stats = stats;
            setOpaque(false);
            Timer timer = new Timer(16, e -> {
                animProgress += 0.025f;
                if (animProgress >= 1f) {
                    animProgress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int labelW = 160;
            int barH = 22;
            int spacing = 36;
            int startY = 20;

            for (int i = 0; i < stats.size(); i++) {
                PerformanceStats s = stats.get(i);
                Category cat = Category.fromString(s.getCategory());
                String name = cat != null ? cat.getDisplayName() : s.getCategory();
                Color color = cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY;
                double acc = s.getTotalAttempted() > 0 ? s.getAccuracy() : 0;

                int y = startY + i * spacing;

                // Label
                g2.setColor(Constants.TEXT_PRIMARY);
                g2.setFont(Constants.FONT_BODY);
                g2.drawString(name, 0, y + barH / 2 + 5);

                // Background bar
                int barX = labelW;
                int barW = w - labelW - 80;
                g2.setColor(Constants.withAlpha(Constants.BORDER_COLOR, 60));
                g2.fillRoundRect(barX, y, barW, barH, 10, 10);

                // Filled bar (animated)
                int fillW = (int)(barW * (acc / 100.0) * animProgress);
                if (fillW > 0) {
                    GradientPaint gp = new GradientPaint(barX, 0, color, barX + fillW, 0,
                            Constants.withAlpha(color, 180));
                    g2.setPaint(gp);
                    g2.fillRoundRect(barX, y, fillW, barH, 10, 10);

                    // Glow effect
                    g2.setPaint(new GradientPaint(barX, y - 4, Constants.withAlpha(color, 30),
                            barX, y + barH + 4, new Color(0, 0, 0, 0)));
                    g2.fillRoundRect(barX, y - 2, fillW, barH + 4, 12, 12);
                }

                // Percentage text
                g2.setColor(acc >= 50 ? Constants.ACCENT_SUCCESS : (acc > 0 ? Constants.ACCENT_WARNING : Constants.TEXT_MUTED));
                g2.setFont(Constants.FONT_BODY_BOLD);
                String accStr = s.getTotalAttempted() > 0 ? String.format("%.1f%%", acc * animProgress) : "—";
                g2.drawString(accStr, barX + barW + 10, y + barH / 2 + 5);
            }

            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════
    private JPanel createCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
    }

    private String formatStudyTime(int totalSeconds) {
        if (totalSeconds < 60) return totalSeconds + "s";
        if (totalSeconds < 3600) return (totalSeconds / 60) + "m";
        return String.format("%dh %dm", totalSeconds / 3600, (totalSeconds % 3600) / 60);
    }
}
