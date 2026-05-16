package com.interviewmentor.view;

import com.interviewmentor.model.User;
import com.interviewmentor.service.AchievementService;
import com.interviewmentor.service.StreakService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Top-level frame with gradient sidebar, header bar, and card content area.
 */
public class MainFrame extends JFrame {

    private CardLayout rootCardLayout;
    private JPanel rootPanel;

    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private CategoryPanel categoryPanel;
    private QuizPanel quizPanel;
    private ResultPanel resultPanel;
    private PerformancePanel performancePanel;
    private HistoryPanel historyPanel;
    private BookmarkPanel bookmarkPanel;
    private AchievementPanel achievementPanel;
    private ProfilePanel profilePanel;
    private ResourcesPanel resourcesPanel;
    private SimulationPanel simulationPanel;
    private FlashcardPanel flashcardPanel;
    private ReviewPanel reviewPanel;
    private SettingsPanel settingsPanel;
    private AnalyticsPanel analyticsPanel;
    private CountdownPanel countdownPanel;
    private BehavioralPanel behavioralPanel;

    private JPanel mainPanel;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private JPanel sidebar;
    private String activePanel = Constants.PANEL_DASHBOARD;
    private java.util.List<JButton> navButtons = new java.util.ArrayList<>();

    // Header
    private JLabel headerUserLabel;
    private JLabel headerStreakLabel;
    private JLabel headerBadgeLabel;
    private JPanel headerAvatarPanel;
    private java.awt.Image headerAvatarImg;

    private User currentUser;
    private final StreakService streakService = new StreakService();
    private final AchievementService achievementService = new AchievementService();

    public MainFrame() {
        setTitle("InterviewMentor \u2014 Technical Interview Preparation");
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(1024, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ToastNotification.init(this);

        rootCardLayout = new CardLayout();
        rootPanel = new JPanel(rootCardLayout);
        rootPanel.setBackground(Constants.BG_PRIMARY);

        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        categoryPanel = new CategoryPanel(this);
        quizPanel = new QuizPanel(this);
        resultPanel = new ResultPanel(this);
        performancePanel = new PerformancePanel(this);
        historyPanel = new HistoryPanel(this);
        bookmarkPanel = new BookmarkPanel(this);
        achievementPanel = new AchievementPanel(this);
        profilePanel = new ProfilePanel(this);
        resourcesPanel = new ResourcesPanel(this);
        simulationPanel = new SimulationPanel(this);
        flashcardPanel = new FlashcardPanel(this);
        reviewPanel = new ReviewPanel(this);
        settingsPanel = new SettingsPanel(this);
        analyticsPanel = new AnalyticsPanel(this);
        countdownPanel = new CountdownPanel(this);
        behavioralPanel = new BehavioralPanel(this);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Constants.BG_PRIMARY);

        buildSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        JPanel header = buildHeaderBar();
        mainPanel.add(header, BorderLayout.NORTH);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Constants.BG_PRIMARY);
        contentPanel.add(dashboardPanel, Constants.PANEL_DASHBOARD);
        contentPanel.add(categoryPanel, Constants.PANEL_CATEGORIES);
        contentPanel.add(quizPanel, Constants.PANEL_QUIZ);
        contentPanel.add(resultPanel, Constants.PANEL_RESULT);
        contentPanel.add(performancePanel, Constants.PANEL_PERFORMANCE);
        contentPanel.add(historyPanel, Constants.PANEL_HISTORY);
        contentPanel.add(bookmarkPanel, Constants.PANEL_BOOKMARKS);
        contentPanel.add(achievementPanel, Constants.PANEL_ACHIEVEMENTS);
        contentPanel.add(profilePanel, Constants.PANEL_PROFILE);
        contentPanel.add(resourcesPanel, Constants.PANEL_RESOURCES);
        contentPanel.add(simulationPanel, Constants.PANEL_SIMULATION);
        contentPanel.add(flashcardPanel, Constants.PANEL_FLASHCARDS);
        contentPanel.add(reviewPanel, Constants.PANEL_REVIEW);
        contentPanel.add(settingsPanel, Constants.PANEL_SETTINGS);
        contentPanel.add(analyticsPanel, Constants.PANEL_ANALYTICS);
        contentPanel.add(countdownPanel, Constants.PANEL_COUNTDOWN);
        contentPanel.add(behavioralPanel, Constants.PANEL_BEHAVIORAL);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        rootPanel.add(loginPanel, "login");
        rootPanel.add(mainPanel, "main");
        rootCardLayout.show(rootPanel, "login");
        setContentPane(rootPanel);
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Constants.BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle gradient highlight at bottom
                g2.setPaint(new GradientPaint(0, getHeight()-2, Constants.withAlpha(Constants.ACCENT_PRIMARY, 20),
                    0, getHeight(), Constants.withAlpha(Constants.ACCENT_PRIMARY, 0)));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, Constants.HEADER_HEIGHT));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel breadcrumb = new JLabel("InterviewMentor");
        breadcrumb.setFont(Constants.FONT_BODY_BOLD);
        breadcrumb.setForeground(Constants.TEXT_MUTED);
        header.add(breadcrumb, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        // Badge count (painted trophy icon)
        JPanel badgeIconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.ACCENT_WARNING);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                // Trophy cup
                g2.drawArc(2, 1, 12, 10, 0, 180);
                g2.drawLine(5, 11, 11, 11);
                g2.drawLine(8, 6, 8, 13);
                g2.drawLine(4, 13, 12, 13);
                // Trophy handles
                g2.drawArc(-1, 2, 6, 6, 90, 180);
                g2.drawArc(11, 2, 6, 6, 270, 180);
                g2.dispose();
            }
        };
        badgeIconPanel.setOpaque(false);
        badgeIconPanel.setPreferredSize(new Dimension(16, 16));
        rightPanel.add(badgeIconPanel);
        headerBadgeLabel = new JLabel("0") {
            @Override public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setForeground(enabled ? Constants.ACCENT_WARNING : Constants.TEXT_MUTED);
                setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        headerBadgeLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 12));
        headerBadgeLabel.setForeground(Constants.ACCENT_WARNING);
        headerBadgeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerBadgeLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (headerBadgeLabel.isEnabled()) showPanel(Constants.PANEL_ACHIEVEMENTS); }
        });
        rightPanel.add(headerBadgeLabel);

        rightPanel.add(Box.createHorizontalStrut(4));

        // Streak (painted flame icon)
        JPanel streakIconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Flame shape
                java.awt.geom.GeneralPath flame = new java.awt.geom.GeneralPath();
                flame.moveTo(8, 0);
                flame.curveTo(8, 0, 14, 6, 13, 11);
                flame.curveTo(12, 14, 10, 15, 8, 15);
                flame.curveTo(6, 15, 4, 14, 3, 11);
                flame.curveTo(2, 6, 8, 0, 8, 0);
                flame.closePath();
                g2.setColor(Constants.ACCENT_ORANGE);
                g2.fill(flame);
                // Inner flame
                java.awt.geom.GeneralPath inner = new java.awt.geom.GeneralPath();
                inner.moveTo(8, 5);
                inner.curveTo(8, 5, 11, 9, 10, 12);
                inner.curveTo(9, 14, 7, 14, 6, 12);
                inner.curveTo(5, 9, 8, 5, 8, 5);
                inner.closePath();
                g2.setColor(Constants.ACCENT_WARNING);
                g2.fill(inner);
                g2.dispose();
            }
        };
        streakIconPanel.setOpaque(false);
        streakIconPanel.setPreferredSize(new Dimension(16, 16));
        rightPanel.add(streakIconPanel);
        headerStreakLabel = new JLabel("0");
        headerStreakLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
        headerStreakLabel.setForeground(Constants.ACCENT_ORANGE);
        rightPanel.add(headerStreakLabel);

        // Separator
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Constants.BORDER_COLOR);
                g.drawLine(0, 8, 0, getHeight() - 8);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(1, 30));
        rightPanel.add(sep);

        // User avatar (clickable → profile)
        reloadAvatar(); // Initial load
        headerAvatarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                // Create circular clip
                java.awt.geom.Ellipse2D.Double clip = new java.awt.geom.Ellipse2D.Double(0, 0, getWidth(), getHeight());
                g2.setClip(clip);

                if (headerAvatarImg != null) {
                    g2.drawImage(headerAvatarImg, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_PRIMARY, getWidth(), getHeight(), Constants.GRADIENT_END));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String init = currentUser != null ? currentUser.getUsername().substring(0, 1).toUpperCase() : "?";
                    g2.drawString(init, (getWidth() - fm.stringWidth(init)) / 2, getHeight() / 2 + fm.getAscent() / 3);
                }
                
                // Draw border ring
                g2.setClip(null);
                g2.setColor(Constants.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                g2.dispose();
            }
        };
        headerAvatarPanel.setOpaque(false);
        headerAvatarPanel.setPreferredSize(new Dimension(34, 34));
        headerAvatarPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerAvatarPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (headerAvatarPanel.isEnabled()) showPanel(Constants.PANEL_PROFILE); }
        });
        rightPanel.add(headerAvatarPanel);

        headerUserLabel = new JLabel("User") {
            @Override public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setForeground(enabled ? Constants.TEXT_PRIMARY : Constants.TEXT_MUTED);
                setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        headerUserLabel.setFont(Constants.FONT_BODY_BOLD);
        headerUserLabel.setForeground(Constants.TEXT_PRIMARY);
        headerUserLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerUserLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (headerUserLabel.isEnabled()) showPanel(Constants.PANEL_PROFILE); }
        });
        rightPanel.add(headerUserLabel);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private void buildSidebar() {
        sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Subtle gradient sidebar
                g2.setPaint(new GradientPaint(0, 0, Constants.BG_SIDEBAR,
                    0, getHeight(), new Color(0x0A0F1E)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border
                g2.setColor(Constants.BORDER_COLOR);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH, 60));
        JPanel logoDot = new JPanel() {
            float phase = 0;
            Timer glowTimer = new Timer(50, e -> { phase += 0.05f; repaint(); });
            { glowTimer.start(); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Animated glow
                float glow = (float)(Math.sin(phase) * 0.3 + 0.7);
                int glowAlpha = (int)(glow * 40);
                g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, glowAlpha));
                g2.fillRoundRect(-4, -4, 44, 44, 14, 14);
                g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_PRIMARY, 36, 36, Constants.GRADIENT_END));
                g2.fillRoundRect(0, 0, 36, 36, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("IM", (36 - fm.stringWidth("IM")) / 2, 36 / 2 + fm.getAscent() / 3);
                g2.dispose();
            }
        };
        logoDot.setOpaque(false);
        logoDot.setPreferredSize(new Dimension(44, 44));
        logoPanel.add(logoDot);
        JLabel logoText = new JLabel("InterviewMentor");
        logoText.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 17));
        logoText.setForeground(Constants.TEXT_PRIMARY);
        logoPanel.add(logoText);
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(32));

        // Section label
        addSectionLabel("MAIN");

        // Nav items
        String[][] navItems = {
            {Constants.PANEL_DASHBOARD,   "Dashboard",    "dashboard"},
            {Constants.PANEL_CATEGORIES,  "Categories",   "grid"},
            {Constants.PANEL_SIMULATION,  "Simulation",   "briefcase"},
            {Constants.PANEL_FLASHCARDS,  "Flashcards",   "cards"},
            {Constants.PANEL_REVIEW,      "Daily Review", "repeat"},
            {Constants.PANEL_RESOURCES,   "Resources",    "book"},
            {Constants.PANEL_BOOKMARKS,   "Bookmarks",    "bookmark"},
            {Constants.PANEL_BEHAVIORAL,  "STAR Stories", "star"},
            {Constants.PANEL_COUNTDOWN,   "Countdown",    "calendar"},
        };
        for (String[] item : navItems) {
            sidebar.add(createNavButton(item[0], item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalStrut(16));
        addSectionLabel("ANALYTICS");

        String[][] analyticsItems = {
            {Constants.PANEL_PERFORMANCE, "Performance",  "radar"},
            {Constants.PANEL_HISTORY,     "History",      "clock"},
            {Constants.PANEL_ACHIEVEMENTS,"Achievements", "trophy"},
            {Constants.PANEL_ANALYTICS,   "Deep Analytics", "chart"},
        };
        for (String[] item : analyticsItems) {
            sidebar.add(createNavButton(item[0], item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalStrut(16));
        addSectionLabel("ACCOUNT");
        sidebar.add(createNavButton(Constants.PANEL_PROFILE, "Profile", "user"));
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(createNavButton(Constants.PANEL_SETTINGS, "Settings", "settings"));
        sidebar.add(Box.createVerticalStrut(2));

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = createNavButton("logout", "Logout", "logout");
        logoutBtn.setForeground(Constants.ACCENT_DANGER);
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
    }

    private void addSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 10));
        label.setForeground(Constants.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH, 20));
        sidebar.add(label);
    }

    private JButton createNavButton(String panelName, String label, String iconType) {
        JButton btn = new JButton(label) {
            boolean hov = false;
            float glowAlpha = 0f;
            Timer hoverAnim;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (!isEnabled()) return;
                        hov = true;
                        animateGlow(1f);
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hov = false;
                        animateGlow(0f);
                    }
                });
            }
            void animateGlow(float target) {
                if (hoverAnim != null && hoverAnim.isRunning()) hoverAnim.stop();
                hoverAnim = new Timer(16, evt -> {
                    float diff = target - glowAlpha;
                    if (Math.abs(diff) < 0.05f) {
                        glowAlpha = target;
                        ((Timer) evt.getSource()).stop();
                    } else {
                        glowAlpha += diff * 0.2f;
                    }
                    repaint();
                });
                hoverAnim.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean isActive = panelName.equals(activePanel);
                boolean enabled = isEnabled();
                
                if (isActive) {
                    g2.setPaint(new GradientPaint(4, 0, Constants.withAlpha(Constants.ACCENT_PRIMARY, 30),
                        getWidth() - 8, 0, Constants.withAlpha(Constants.GRADIENT_END, 15)));
                    g2.fill(new RoundRectangle2D.Float(4, 0, getWidth() - 8, getHeight(), 10, 10));
                    g2.setPaint(new GradientPaint(0, 6, Constants.ACCENT_PRIMARY, 0, getHeight() - 6, Constants.GRADIENT_END));
                    g2.fill(new RoundRectangle2D.Float(0, 6, 3, getHeight() - 12, 3, 3));
                } else if (enabled && glowAlpha > 0) {
                    int alpha = (int)(glowAlpha * 12);
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, alpha));
                    g2.fill(new RoundRectangle2D.Float(4, 0, getWidth() - 8, getHeight(), 10, 10));
                }
                
                Color iconColor;
                if (!enabled) {
                    iconColor = Constants.TEXT_MUTED;
                } else {
                    iconColor = isActive ? Constants.ACCENT_PRIMARY_LIGHT
                        : (hov ? Constants.TEXT_PRIMARY : Constants.TEXT_SECONDARY);
                }
                g2.setColor(iconColor);

                // Paint icon shape at (20, cy)
                int iy = getHeight() / 2;
                g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                paintNavIcon(g2, iconType, 16, iy, iconColor);

                // Draw label text
                g2.setColor(iconColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, 36, getHeight() / 2 + fm.getAscent() / 3);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(Constants.FONT_BODY);
        btn.setForeground(Constants.TEXT_SECONDARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH, 42));
        btn.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!panelName.equals("logout")) {
            btn.addActionListener(e -> showPanel(panelName));
        }
        navButtons.add(btn);
        return btn;
    }

    /** Paint a vector icon shape by name at the given center point */
    private void paintNavIcon(Graphics2D g2, String type, int cx, int cy, Color color) {
        g2.setColor(color);
        switch (type) {
            case "dashboard" -> {
                // 4 squares grid
                g2.fillRoundRect(cx - 7, cy - 7, 6, 6, 2, 2);
                g2.fillRoundRect(cx + 1, cy - 7, 6, 6, 2, 2);
                g2.fillRoundRect(cx - 7, cy + 1, 6, 6, 2, 2);
                g2.fillRoundRect(cx + 1, cy + 1, 6, 6, 2, 2);
            }
            case "grid" -> {
                // 9-dot grid
                for (int r = -1; r <= 1; r++)
                    for (int c = -1; c <= 1; c++)
                        g2.fillOval(cx + c * 6 - 2, cy + r * 6 - 2, 4, 4);
            }
            case "book" -> {
                // Open book
                g2.drawLine(cx, cy - 7, cx, cy + 6);
                g2.drawArc(cx - 9, cy - 7, 9, 5, 0, 180);
                g2.drawArc(cx, cy - 7, 9, 5, 0, 180);
                g2.drawLine(cx - 9, cy - 5, cx - 9, cy + 6);
                g2.drawLine(cx + 9, cy - 5, cx + 9, cy + 6);
                g2.drawLine(cx - 9, cy + 6, cx, cy + 4);
                g2.drawLine(cx + 9, cy + 6, cx, cy + 4);
            }
            case "bookmark" -> {
                // Bookmark flag
                int[] xp = {cx - 5, cx - 5, cx, cx + 5, cx + 5};
                int[] yp = {cy - 8, cy + 8, cy + 3, cy + 8, cy - 8};
                g2.drawPolygon(xp, yp, 5);
            }
            case "chart" -> {
                // Bar chart
                g2.fillRect(cx - 7, cy + 1, 4, 6);
                g2.fillRect(cx - 2, cy - 4, 4, 11);
                g2.fillRect(cx + 3, cy - 7, 4, 14);
            }
            case "radar" -> {
                // Radar / spider web chart
                int[] px = {cx, cx+7, cx+4, cx-4, cx-7};
                int[] py = {cy-7, cy-2, cy+6, cy+6, cy-2};
                g2.drawPolygon(px, py, 5);
                int[] px2 = {cx, cx+3, cx+2, cx-2, cx-3};
                int[] py2 = {cy-3, cy-1, cy+3, cy+3, cy-1};
                g2.drawPolygon(px2, py2, 5);
                g2.drawLine(cx, cy-7, cx, cy);
                g2.drawLine(cx+7, cy-2, cx, cy);
                g2.drawLine(cx-7, cy-2, cx, cy);
            }
            case "briefcase" -> {
                // Briefcase for Interview Simulation
                g2.drawRoundRect(cx - 7, cy - 3, 14, 10, 2, 2);
                g2.drawArc(cx - 3, cy - 6, 6, 6, 0, 180);
                g2.drawLine(cx - 7, cy + 1, cx + 7, cy + 1);
                g2.fillRect(cx - 1, cy, 2, 2);
            }
            case "cards" -> {
                // Stack of Flashcards
                g2.drawRoundRect(cx - 6, cy - 4, 9, 11, 2, 2);
                g2.drawLine(cx - 3, cy - 6, cx + 6, cy - 6);
                g2.drawArc(cx + 4, cy - 6, 2, 2, 0, 90);
                g2.drawLine(cx + 6, cy - 4, cx + 6, cy + 5);
                g2.drawArc(cx + 4, cy + 5, 2, 2, 270, 90);
                g2.drawLine(cx - 3, cy - 1, cx + 1, cy - 1);
                g2.drawLine(cx - 3, cy + 2, cx + 1, cy + 2);
            }
            case "repeat" -> {
                // Circular arrows for Spaced Repetition
                g2.drawArc(cx - 6, cy - 6, 12, 12, 60, 240);
                g2.drawLine(cx + 6, cy - 6, cx + 3, cy - 3);
                g2.drawLine(cx + 6, cy - 6, cx + 9, cy - 3);
                g2.drawArc(cx - 3, cy - 3, 6, 6, 0, 360);
            }
            case "clock" -> {
                // Clock face
                g2.drawOval(cx - 7, cy - 7, 14, 14);
                g2.drawLine(cx, cy, cx, cy - 4);
                g2.drawLine(cx, cy, cx + 3, cy + 2);
            }
            case "trophy" -> {
                // Trophy cup
                g2.drawArc(cx - 6, cy - 7, 12, 9, 0, 180);
                g2.drawLine(cx, cy - 1, cx, cy + 4);
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy + 4);
                g2.drawLine(cx - 5, cy + 6, cx + 5, cy + 6);
                g2.drawArc(cx - 9, cy - 5, 5, 5, 90, 180);
                g2.drawArc(cx + 4, cy - 5, 5, 5, 270, 180);
            }
            case "user" -> {
                // User silhouette
                g2.drawOval(cx - 4, cy - 8, 8, 8);
                g2.drawArc(cx - 7, cy + 1, 14, 10, 0, 180);
            }
            case "settings" -> {
                // Gear icon
                g2.drawOval(cx - 4, cy - 4, 8, 8);
                for(int i=0; i<8; i++) {
                    double a = i * Math.PI / 4.0;
                    g2.drawLine(cx + (int)(4*Math.cos(a)), cy + (int)(4*Math.sin(a)),
                                cx + (int)(6*Math.cos(a)), cy + (int)(6*Math.sin(a)));
                }
            }
            case "calendar" -> {
                // Calendar icon
                g2.drawRoundRect(cx - 7, cy - 5, 14, 12, 2, 2);
                g2.drawLine(cx - 7, cy - 1, cx + 7, cy - 1);
                g2.drawLine(cx - 3, cy - 8, cx - 3, cy - 4);
                g2.drawLine(cx + 3, cy - 8, cx + 3, cy - 4);
            }
            case "star" -> {
                // Star icon
                int[] xPoints = {cx, cx+2, cx+7, cx+3, cx+4, cx, cx-4, cx-3, cx-7, cx-2};
                int[] yPoints = {cy-7, cy-2, cy-2, cy+1, cy+6, cy+3, cy+6, cy+1, cy-2, cy-2};
                g2.drawPolygon(xPoints, yPoints, 10);
            }
            case "logout" -> {
                // Arrow leaving door
                g2.setColor(Constants.ACCENT_DANGER);
                g2.drawRect(cx - 7, cy - 7, 8, 14);
                g2.drawLine(cx + 1, cy, cx + 8, cy);
                g2.drawLine(cx + 5, cy - 3, cx + 8, cy);
                g2.drawLine(cx + 5, cy + 3, cx + 8, cy);
            }
        }
    }

    public void reloadAvatar() {
        try {
            java.io.File extFile = new java.io.File("user_avatar.png");
            if (extFile.exists()) {
                headerAvatarImg = javax.imageio.ImageIO.read(extFile);
            } else {
                java.io.InputStream is = getClass().getResourceAsStream("/images/avatar.png");
                if (is != null) {
                    headerAvatarImg = javax.imageio.ImageIO.read(is);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (headerAvatarPanel != null) {
            headerAvatarPanel.repaint();
        }
    }

    public void showPanel(String panelName) {
        // Block navigation AWAY from active Quiz/Simulation
        boolean isQuizRunning = quizPanel != null && quizPanel.isRunning();
        boolean isSimRunning = simulationPanel != null && simulationPanel.isRunning();
        
        if (isQuizRunning || isSimRunning) {
            // Allow going TO the quiz/simulation (initial load) or TO the results
            boolean goingToQuiz = panelName.equals(Constants.PANEL_QUIZ);
            boolean goingToSim = panelName.equals(Constants.PANEL_SIMULATION);
            boolean goingToResult = panelName.equals(Constants.PANEL_RESULT);
            
            if (!goingToQuiz && !goingToSim && !goingToResult) {
                ToastNotification.show("STRICT MODE: Finish your session before navigating away!", ToastNotification.Type.ERROR);
                return;
            }
        }

        activePanel = panelName;
        contentCardLayout.show(contentPanel, panelName);
        switch (panelName) {
            case Constants.PANEL_DASHBOARD -> dashboardPanel.refresh();
            case Constants.PANEL_SIMULATION -> simulationPanel.refresh();
            case Constants.PANEL_FLASHCARDS -> flashcardPanel.refresh();
            case Constants.PANEL_REVIEW -> reviewPanel.refresh();
            case Constants.PANEL_PERFORMANCE -> performancePanel.refresh();
            case Constants.PANEL_HISTORY -> historyPanel.refresh();
            case Constants.PANEL_BOOKMARKS -> bookmarkPanel.refresh();
            case Constants.PANEL_ACHIEVEMENTS -> achievementPanel.refresh();
            case Constants.PANEL_PROFILE -> profilePanel.refresh();
            case Constants.PANEL_RESOURCES -> resourcesPanel.refresh();
            case Constants.PANEL_ANALYTICS -> analyticsPanel.refresh();
            case Constants.PANEL_COUNTDOWN -> countdownPanel.refresh();
            case Constants.PANEL_BEHAVIORAL -> behavioralPanel.refresh();
        }
        sidebar.repaint();
    }

    public void setSidebarEnabled(boolean enabled) {
        for (JButton btn : navButtons) {
            btn.setEnabled(enabled);
        }
        headerBadgeLabel.setEnabled(enabled);
        headerUserLabel.setEnabled(enabled);
        headerAvatarPanel.setEnabled(enabled);
        sidebar.repaint();
    }

    public void onLoginSuccess(User user) {
        this.currentUser = user;
        headerUserLabel.setText(user.getUsername());
        updateHeaderStreak();
        updateHeaderBadges();
        rootCardLayout.show(rootPanel, "main");
        loginPanel.stopAnimation();
        showPanel(Constants.PANEL_DASHBOARD);
        ToastNotification.success("Welcome back, " + user.getUsername() + "!");
    }

    public void updateHeaderStreak() {
        if (currentUser != null) {
            int streak = streakService.getCurrentStreak(currentUser.getId());
            headerStreakLabel.setText(String.valueOf(streak));
        }
    }

    public void updateHeaderBadges() {
        if (currentUser != null) {
            int count = achievementService.getEarnedCount(currentUser.getId());
            headerBadgeLabel.setText(String.valueOf(count));
        }
    }

    public void logout() {
        if ((quizPanel != null && quizPanel.isRunning()) || (simulationPanel != null && simulationPanel.isRunning())) {
            ToastNotification.show("STRICT MODE: Cannot logout during active session!", ToastNotification.Type.ERROR);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            quizPanel.stopTimer();
            simulationPanel.stopTimer();
            loginPanel.startAnimation();
            rootCardLayout.show(rootPanel, "login");
        }
    }

    public User getCurrentUser()            { return currentUser; }
    public QuizPanel getQuizPanel()         { return quizPanel; }
    public ResultPanel getResultPanel()     { return resultPanel; }
    public StreakService getStreakService()  { return streakService; }
    public AchievementService getAchievementService() { return achievementService; }
}
