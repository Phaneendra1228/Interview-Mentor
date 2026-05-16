package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.service.AnalyticsService;
import com.interviewmentor.service.StreakService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.BarChart;
import com.interviewmentor.view.components.CircularProgress;
import com.interviewmentor.view.components.HeatmapCalendar;
import com.interviewmentor.view.components.RadarChart;
import com.interviewmentor.view.components.SparklineChart;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance analytics with animated counters, trend sparkline, bar chart, and weak areas.
 */
public class PerformancePanel extends JPanel {

    private final MainFrame mainFrame;
    private final AnalyticsService analytics = new AnalyticsService();
    private final StreakService streakService = new StreakService();
    private JPanel contentPanel;

    public PerformancePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    public void refresh() {
        removeAll();
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        int userId = mainFrame.getCurrentUser().getId();

        JLabel title = new JLabel("Performance Analytics");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(title);

        JLabel subtitle = new JLabel("Track your progress and identify areas for improvement");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subtitle);
        contentPanel.add(Box.createVerticalStrut(28));

        PerformanceStats overall = analytics.getOverallStats(userId);
        int totalQuizzes = analytics.getTotalQuizCount(userId);

        // Top row
        JPanel topRow = new JPanel(new GridLayout(1, 2, 20, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Circular progress card
        JPanel circleCard = createCard();
        circleCard.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        CircularProgress overallCircle = new CircularProgress(170);
        double acc = overall.getAccuracy();
        overallCircle.setProgressColor(acc >= 70 ? Constants.ACCENT_SUCCESS : acc >= 40 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER);
        overallCircle.setLabel("Overall");
        overallCircle.setStrokeWidth(14);
        overallCircle.setPercentage(acc);
        circleCard.add(overallCircle);

        JPanel overallStats = new JPanel();
        overallStats.setOpaque(false);
        overallStats.setLayout(new BoxLayout(overallStats, BoxLayout.Y_AXIS));
        overallStats.add(createAnimatedStat("Total Quizzes", totalQuizzes));
        overallStats.add(Box.createVerticalStrut(10));
        overallStats.add(createAnimatedStat("Questions", overall.getTotalAttempted()));
        overallStats.add(Box.createVerticalStrut(10));
        overallStats.add(createAnimatedStat("Correct", overall.getCorrectCount()));
        overallStats.add(Box.createVerticalStrut(10));
        overallStats.add(createMiniStat("Avg Time/Q", Constants.formatTime((int) overall.getAvgTimeSeconds())));
        circleCard.add(overallStats);
        topRow.add(circleCard);

        // Bar chart card
        JPanel barCard = createCard();
        barCard.setLayout(new BorderLayout());
        barCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        BarChart barChart = new BarChart();
        barChart.setTitle("Accuracy by Category");
        barChart.setMaxValue(100);

        List<PerformanceStats> catStats = analytics.getCategoryStats(userId);
        for (PerformanceStats stat : catStats) {
            if (stat.getTotalAttempted() > 0) {
                Category cat = Category.fromString(stat.getCategory());
                String label = cat != null ? cat.getDisplayName() : stat.getCategory();
                Color color = cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY;
                barChart.addBar(label, stat.getAccuracy(), color);
            }
        }
        barChart.animateBars();
        barCard.add(barChart, BorderLayout.CENTER);
        topRow.add(barCard);

        contentPanel.add(topRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // Radar chart + Heatmap row
        JPanel analyticsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        analyticsRow.setOpaque(false);
        analyticsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        analyticsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Radar chart card
        JPanel radarCard = createCard();
        radarCard.setLayout(new BorderLayout());
        radarCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel radarTitle = new JLabel("Skill Profile");
        radarTitle.setFont(Constants.FONT_SUBHEADING);
        radarTitle.setForeground(Constants.TEXT_PRIMARY);
        radarTitle.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 0));
        radarCard.add(radarTitle, BorderLayout.NORTH);
        RadarChart radar = new RadarChart();
        for (PerformanceStats stat : catStats) {
            Category rCat = Category.fromString(stat.getCategory());
            if (rCat != null) {
                double catAcc = stat.getTotalAttempted() > 0 ? stat.getAccuracy() : 0;
                radar.addAxis(rCat.getShortCode(), catAcc, rCat.getColor());
            }
        }
        radar.animate();
        radarCard.add(radar, BorderLayout.CENTER);
        analyticsRow.add(radarCard);

        // Heatmap calendar card
        JPanel heatmapCard = createCard();
        heatmapCard.setLayout(new BorderLayout());
        heatmapCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel heatTitle = new JLabel("Activity Heatmap (16 weeks)");
        heatTitle.setFont(Constants.FONT_SUBHEADING);
        heatTitle.setForeground(Constants.TEXT_PRIMARY);
        heatTitle.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 0));
        heatmapCard.add(heatTitle, BorderLayout.NORTH);
        HeatmapCalendar heatmap = new HeatmapCalendar();
        java.util.Map<String, Integer> heatData = streakService.getActivityHeatmap(userId, 112);
        heatmap.setData(heatData);
        heatmapCard.add(heatmap, BorderLayout.CENTER);
        analyticsRow.add(heatmapCard);

        contentPanel.add(analyticsRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // Accuracy trend card
        double[] recentScores = streakService.getRecentScores(userId, 15);
        if (recentScores.length >= 2) {
            JPanel trendCard = createCard();
            trendCard.setLayout(new BorderLayout());
            trendCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            trendCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            trendCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel trendTitle = new JLabel("Accuracy Trend (Last " + recentScores.length + " Quizzes)");
            trendTitle.setFont(Constants.FONT_SUBHEADING);
            trendTitle.setForeground(Constants.TEXT_PRIMARY);
            trendCard.add(trendTitle, BorderLayout.NORTH);

            SparklineChart trendChart = new SparklineChart(0, 100);
            trendChart.setStrokeWidth(3);
            trendChart.setLineColor(Constants.ACCENT_PRIMARY_LIGHT);
            List<Double> scoreList = new ArrayList<>();
            for (double s : recentScores) scoreList.add(s);
            trendChart.setData(scoreList);
            trendChart.setPreferredSize(new Dimension(0, 100));
            trendCard.add(trendChart, BorderLayout.CENTER);

            contentPanel.add(trendCard);
            contentPanel.add(Box.createVerticalStrut(24));
        }

        // Weak areas
        List<PerformanceStats> weakAreas = analytics.getWeakAreas(userId);
        if (!weakAreas.isEmpty()) {
            JLabel weakTitle = new JLabel("Areas for Improvement");
            weakTitle.setFont(Constants.FONT_HEADING);
            weakTitle.setForeground(Constants.ACCENT_WARNING);
            weakTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(weakTitle);
            contentPanel.add(Box.createVerticalStrut(12));
            for (PerformanceStats ws : weakAreas) {
                contentPanel.add(createWeakAreaCard(ws));
                contentPanel.add(Box.createVerticalStrut(8));
            }
        } else if (totalQuizzes > 0) {
            JLabel goodJob = new JLabel("[OK] Great job! No weak areas detected. Keep practicing!");
            goodJob.setFont(Constants.FONT_BODY);
            goodJob.setForeground(Constants.ACCENT_SUCCESS);
            goodJob.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(goodJob);
        } else {
            JLabel noData = new JLabel("Take some quizzes to see your performance analytics!");
            noData.setFont(Constants.FONT_BODY);
            noData.setForeground(Constants.TEXT_MUTED);
            noData.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(noData);
        }

        // Category detail cards
        contentPanel.add(Box.createVerticalStrut(24));
        JLabel detailTitle = new JLabel("Category Details");
        detailTitle.setFont(Constants.FONT_HEADING);
        detailTitle.setForeground(Constants.TEXT_PRIMARY);
        detailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(detailTitle);
        contentPanel.add(Box.createVerticalStrut(12));

        JPanel detailGrid = new JPanel(new GridLayout(0, 4, 12, 12));
        detailGrid.setOpaque(false);
        detailGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        for (PerformanceStats stat : catStats) {
            detailGrid.add(createCategoryDetailCard(stat));
        }
        contentPanel.add(detailGrid);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
    }

    /** Creates a stat label with animated count-up from 0 */
    private JPanel createAnimatedStat(String label, int targetValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(180, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Constants.FONT_SMALL);
        lbl.setForeground(Constants.TEXT_MUTED);
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel("0");
        val.setFont(Constants.FONT_BODY_BOLD);
        val.setForeground(Constants.TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);

        // Animate count up
        Timer countTimer = new Timer(30, null);
        final int[] current = {0};
        countTimer.addActionListener(e -> {
            int step = Math.max(1, (targetValue - current[0]) / 8);
            current[0] += step;
            if (current[0] >= targetValue) {
                current[0] = targetValue;
                countTimer.stop();
            }
            val.setText(String.valueOf(current[0]));
        });
        // Delay start
        Timer delayTimer = new Timer(300, e -> countTimer.start());
        delayTimer.setRepeats(false);
        delayTimer.start();

        return row;
    }

    private JPanel createMiniStat(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(180, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(Constants.FONT_SMALL);
        lbl.setForeground(Constants.TEXT_MUTED);
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(Constants.FONT_BODY_BOLD);
        val.setForeground(Constants.TEXT_PRIMARY);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel createWeakAreaCard(PerformanceStats stat) {
        Category cat = Category.fromString(stat.getCategory());
        String catName = cat != null ? cat.getDisplayName() : stat.getCategory();
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(Constants.ACCENT_WARNING, 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Constants.withAlpha(Constants.ACCENT_WARNING, 40));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel nameLabel = new JLabel(catName);
        nameLabel.setFont(Constants.FONT_BODY_BOLD);
        nameLabel.setForeground(Constants.ACCENT_WARNING);
        card.add(nameLabel, BorderLayout.WEST);
        JLabel accLabel = new JLabel(Constants.formatPercentage(stat.getAccuracy()) +
            " accuracy (" + stat.getCorrectCount() + "/" + stat.getTotalAttempted() + ")");
        accLabel.setFont(Constants.FONT_SMALL);
        accLabel.setForeground(Constants.TEXT_SECONDARY);
        card.add(accLabel, BorderLayout.EAST);
        return card;
    }

    private JPanel createCategoryDetailCard(PerformanceStats stat) {
        Category cat = Category.fromString(stat.getCategory());
        Color catColor = cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY;
        String catName = cat != null ? cat.getShortCode() : "??";
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(catColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel code = new JLabel(catName);
        code.setFont(Constants.FONT_BODY_BOLD);
        code.setForeground(catColor);
        code.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(code);
        card.add(Box.createVerticalStrut(4));
        JLabel attempted = new JLabel("Attempted: " + stat.getTotalAttempted());
        attempted.setFont(Constants.FONT_SMALL);
        attempted.setForeground(Constants.TEXT_MUTED);
        attempted.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(attempted);
        JLabel accuracy = new JLabel("Accuracy: " + (stat.getTotalAttempted() > 0 ? Constants.formatPercentage(stat.getAccuracy()) : "N/A"));
        accuracy.setFont(Constants.FONT_SMALL);
        accuracy.setForeground(stat.getTotalAttempted() > 0
            ? (stat.getAccuracy() >= 50 ? Constants.ACCENT_SUCCESS : Constants.ACCENT_DANGER)
            : Constants.TEXT_MUTED);
        accuracy.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(accuracy);
        return card;
    }
}
