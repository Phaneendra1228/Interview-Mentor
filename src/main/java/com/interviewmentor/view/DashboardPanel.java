package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.model.QuizSession;
import com.interviewmentor.model.User;
import com.interviewmentor.service.AnalyticsService;
import com.interviewmentor.service.QuizService;
import com.interviewmentor.service.StreakService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.SparklineChart;
import com.interviewmentor.view.components.StreakTracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard with animated stats cards, streak, sparklines, recommendations, and recent activity.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AnalyticsService analytics = new AnalyticsService();
    private final QuizService quizService = new QuizService();
    private final StreakService streakService = new StreakService();

    private JLabel welcomeLabel;
    private JPanel statsGrid;
    private JPanel recentPanel;
    private JPanel recommendPanel;
    private StreakTracker streakTracker;
    private JLabel quoteLabel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Welcome header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel welcomeBox = new JPanel();
        welcomeBox.setOpaque(false);
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeLabel = new JLabel("Welcome back!");
        welcomeLabel.setFont(Constants.FONT_TITLE);
        welcomeLabel.setForeground(Constants.TEXT_PRIMARY);
        welcomeBox.add(welcomeLabel);
        JLabel subLabel = new JLabel("Ready to ace your next interview?");
        subLabel.setFont(Constants.FONT_BODY);
        subLabel.setForeground(Constants.TEXT_SECONDARY);
        welcomeBox.add(subLabel);
        headerRow.add(welcomeBox, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        RoundedButton digestBtn = new RoundedButton("📊 Weekly Digest");
        digestBtn.setPreferredSize(new Dimension(170, 48));
        digestBtn.setBackground(Constants.ACCENT_INFO);
        digestBtn.addActionListener(e -> {
            WeeklyDigestDialog dialog = new WeeklyDigestDialog(mainFrame);
            dialog.setVisible(true);
        });
        btnRow.add(digestBtn);

        RoundedButton startBtn = new RoundedButton("Start New Quiz");
        startBtn.setPreferredSize(new Dimension(180, 48));
        startBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_CATEGORIES));
        btnRow.add(startBtn);

        headerRow.add(btnRow, BorderLayout.EAST);

        content.add(headerRow);
        content.add(Box.createVerticalStrut(16));

        // Quote card
        JPanel quoteCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Constants.withAlpha(Constants.ACCENT_PRIMARY, 15),
                    getWidth(), 0, Constants.withAlpha(Constants.GRADIENT_END, 15)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        quoteCard.setOpaque(false);
        quoteCard.setLayout(new BorderLayout());
        quoteCard.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        quoteCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        quoteCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        quoteLabel = new JLabel(Constants.getDailyQuote());
        quoteLabel.setFont(new Font(Constants.FONT_FAMILY, Font.ITALIC, 13));
        quoteLabel.setForeground(Constants.TEXT_SECONDARY);
        quoteCard.add(quoteLabel, BorderLayout.CENTER);
        content.add(quoteCard);
        content.add(Box.createVerticalStrut(20));

        // Stats cards row
        statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statsGrid);
        content.add(Box.createVerticalStrut(20));

        // Streak tracker
        streakTracker = new StreakTracker();
        streakTracker.setAlignmentX(Component.LEFT_ALIGNMENT);
        streakTracker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        content.add(streakTracker);
        content.add(Box.createVerticalStrut(24));

        // Recommendations section
        JPanel recHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        recHeader.setOpaque(false);
        recHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel starIconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.ACCENT_WARNING);
                // 5-point star shape
                int cx = 11, cy = 11;
                int[] sx = new int[10], sy = new int[10];
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI / 2 + i * Math.PI / 5;
                    int r = (i % 2 == 0) ? 10 : 4;
                    sx[i] = cx + (int)(r * Math.cos(angle));
                    sy[i] = cy - (int)(r * Math.sin(angle));
                }
                g2.fillPolygon(sx, sy, 10);
                g2.dispose();
            }
        };
        starIconPanel.setOpaque(false);
        starIconPanel.setPreferredSize(new Dimension(22, 22));
        JLabel recTitleText = new JLabel("Recommended for You");
        recTitleText.setFont(Constants.FONT_HEADING);
        recTitleText.setForeground(Constants.TEXT_PRIMARY);
        recHeader.add(starIconPanel);
        recHeader.add(recTitleText);
        content.add(recHeader);
        content.add(Box.createVerticalStrut(10));

        recommendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        recommendPanel.setOpaque(false);
        recommendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        recommendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        content.add(recommendPanel);
        content.add(Box.createVerticalStrut(24));

        // Recent quizzes
        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(Constants.FONT_HEADING);
        recentTitle.setForeground(Constants.TEXT_PRIMARY);
        recentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(recentTitle);
        content.add(Box.createVerticalStrut(12));

        recentPanel = new JPanel();
        recentPanel.setOpaque(false);
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(recentPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user == null) return;

        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        quoteLabel.setText(Constants.getDailyQuote());

        statsGrid.removeAll();
        PerformanceStats overall = analytics.getOverallStats(user.getId());
        int totalQuizzes = analytics.getTotalQuizCount(user.getId());
        String bestCat = analytics.getBestCategory(user.getId());

        double[] recentScores = streakService.getRecentScores(user.getId(), 10);
        List<Double> scoreList = new ArrayList<>();
        for (double s : recentScores) scoreList.add(s);

        statsGrid.add(createStatCard("Total Quizzes", String.valueOf(totalQuizzes),
            Constants.ACCENT_PRIMARY, "quiz", scoreList, Constants.ACCENT_PRIMARY, 0));
        statsGrid.add(createStatCard("Accuracy", Constants.formatPercentage(overall.getAccuracy()),
            Constants.ACCENT_SUCCESS, "check", scoreList, Constants.ACCENT_SUCCESS, 80));
        statsGrid.add(createStatCard("Questions Done", String.valueOf(overall.getTotalAttempted()),
            Constants.ACCENT_INFO, "list", null, null, 160));
        statsGrid.add(createStatCard("Best Topic", bestCat,
            Constants.ACCENT_WARNING, "trophy", null, null, 240));
        statsGrid.revalidate();
        statsGrid.repaint();

        // Streak
        int[] activity = streakService.getLast7DaysActivity(user.getId());
        int currentStreak = streakService.getCurrentStreak(user.getId());
        int longestStreak = streakService.getLongestStreak(user.getId());
        streakTracker.setData(activity, currentStreak, longestStreak);

        // Recommendations
        recommendPanel.removeAll();
        List<PerformanceStats> weakAreas = analytics.getWeakAreas(user.getId());
        if (weakAreas.isEmpty() && totalQuizzes == 0) {
            for (Category cat : new Category[]{Category.DATA_STRUCTURES, Category.ALGORITHMS, Category.JAVA}) {
                recommendPanel.add(createRecommendChip(cat, "Start"));
            }
        } else {
            for (int i = 0; i < Math.min(3, weakAreas.size()); i++) {
                Category cat = Category.fromString(weakAreas.get(i).getCategory());
                if (cat != null) {
                    recommendPanel.add(createRecommendChip(cat,
                        Constants.formatPercentage(weakAreas.get(i).getAccuracy())));
                }
            }
        }
        recommendPanel.revalidate();
        recommendPanel.repaint();

        // Recent
        recentPanel.removeAll();
        List<QuizSession> recent = quizService.getRecentSessions(user.getId(), 5);
        if (recent.isEmpty()) {
            JLabel empty = new JLabel("No quizzes taken yet. Start your first quiz!");
            empty.setFont(Constants.FONT_BODY);
            empty.setForeground(Constants.TEXT_MUTED);
            recentPanel.add(empty);
        } else {
            for (QuizSession session : recent) {
                recentPanel.add(createRecentRow(session));
                recentPanel.add(Box.createVerticalStrut(8));
            }
        }
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    private JPanel createRecommendChip(Category cat, String info) {
        JPanel chip = new JPanel() {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; setCursor(new Cursor(Cursor.HAND_CURSOR)); repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hov = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { mainFrame.showPanel(Constants.PANEL_CATEGORIES); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hov ? Constants.withAlpha(cat.getColor(), 30) : Constants.withAlpha(cat.getColor(), 15);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Constants.withAlpha(cat.getColor(), 60));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        chip.setPreferredSize(new Dimension(180, 65));

        JLabel name = new JLabel(cat.getDisplayName());
        name.setFont(Constants.FONT_BODY_BOLD);
        name.setForeground(cat.getColor());
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        chip.add(name);

        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(Constants.FONT_SMALL);
        infoLabel.setForeground(Constants.TEXT_MUTED);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chip.add(infoLabel);

        return chip;
    }

    private JPanel createStatCard(String title, String value, Color accent, String icon,
                                   List<Double> sparkData, Color sparkColor, int animDelay) {
        JPanel card = new JPanel() {
            float slideOffset = 20f;
            float opacity = 0f;
            Timer entryAnim;
            {
                entryAnim = new Timer(16, null);
                Timer delay = new Timer(animDelay, e -> {
                    entryAnim.addActionListener(evt -> {
                        slideOffset *= 0.85f;
                        opacity = Math.min(1f, opacity + 0.08f);
                        if (slideOffset < 0.5f && opacity >= 1f) {
                            slideOffset = 0;
                            opacity = 1f;
                            entryAnim.stop();
                        }
                        repaint();
                    });
                    entryAnim.start();
                });
                delay.setRepeats(false);
                delay.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacity)));
                g2.translate(0, (int)slideOffset);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // Paint icon instead of text
        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 10, cy = 10;
                switch (icon) {
                    case "quiz" -> { g2.fillRoundRect(cx-7,cy-7,6,6,2,2); g2.fillRoundRect(cx+1,cy-7,6,6,2,2); g2.fillRoundRect(cx-7,cy+1,6,6,2,2); g2.fillRoundRect(cx+1,cy+1,6,6,2,2); }
                    case "check" -> { g2.drawLine(cx-5,cy,cx-2,cy+4); g2.drawLine(cx-2,cy+4,cx+6,cy-5); }
                    case "list" -> { g2.drawLine(cx-6,cy-5,cx+6,cy-5); g2.drawLine(cx-6,cy,cx+6,cy); g2.drawLine(cx-6,cy+5,cx+6,cy+5); }
                    case "trophy" -> { g2.drawArc(cx-5,cy-6,10,8,0,180); g2.drawLine(cx,cy-1,cx,cy+3); g2.drawLine(cx-3,cy+5,cx+3,cy+5); }
                }
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(20, 20));
        iconPanel.setMaximumSize(new Dimension(20, 20));
        iconPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(iconPanel);
        infoPanel.add(Box.createVerticalStrut(6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Constants.FONT_SMALL);
        titleLbl.setForeground(Constants.TEXT_MUTED);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(titleLbl);
        infoPanel.add(Box.createVerticalStrut(2));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 22));
        valueLbl.setForeground(Constants.TEXT_PRIMARY);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(valueLbl);

        card.add(infoPanel, BorderLayout.CENTER);

        if (sparkData != null && sparkData.size() >= 2) {
            SparklineChart sparkline = new SparklineChart(80, 35);
            sparkline.setLineColor(sparkColor != null ? sparkColor : accent);
            sparkline.setData(sparkData);
            card.add(sparkline, BorderLayout.EAST);
        }

        return card;
    }

    private JPanel createRecentRow(QuizSession session) {
        JPanel row = new JPanel() {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Constants.BG_CARD_HOVER : Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setLayout(new BorderLayout(16, 0));
        row.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        Category cat = Category.fromString(session.getCategory());
        String catName = cat != null ? cat.getDisplayName() : session.getCategory();

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY);
                g2.fillOval(2, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 14));
        leftPanel.add(dot);

        JLabel catLabel = new JLabel(catName);
        catLabel.setFont(Constants.FONT_BODY_BOLD);
        catLabel.setForeground(Constants.TEXT_PRIMARY);
        leftPanel.add(catLabel);

        if (session.getDifficulty() != null) {
            JLabel diffLabel = new JLabel(" \u00B7 " + session.getDifficulty());
            diffLabel.setFont(Constants.FONT_SMALL);
            diffLabel.setForeground(Constants.TEXT_MUTED);
            leftPanel.add(diffLabel);
        }
        row.add(leftPanel, BorderLayout.WEST);

        String dateStr = "";
        if (session.getStartedAt() != null) {
            dateStr = session.getStartedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        }
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(Constants.FONT_SMALL);
        dateLabel.setForeground(Constants.TEXT_MUTED);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(dateLabel, BorderLayout.CENTER);

        double acc = session.getAccuracy();
        Color scoreColor = acc >= 70 ? Constants.ACCENT_SUCCESS : acc >= 40 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER;
        JLabel scoreLabel = new JLabel(session.getCorrectAnswers() + "/" + session.getTotalQuestions()
            + "  (" + Constants.formatPercentage(acc) + ")");
        scoreLabel.setFont(Constants.FONT_BODY_BOLD);
        scoreLabel.setForeground(scoreColor);
        row.add(scoreLabel, BorderLayout.EAST);

        return row;
    }
}
