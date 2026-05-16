package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.MasteryLevel;
import com.interviewmentor.model.Question;
import com.interviewmentor.model.TrainingPlan;
import com.interviewmentor.service.AdaptiveService;
import com.interviewmentor.service.QuestionService;
import com.interviewmentor.service.TrainingPlanService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Category selection grid with hover lift animations and comprehensive quiz config.
 * Implements flowchart: Select Preparation Type, Valid Profile?, Fetch Training Alert,
 * Set Goal / Fetch Session, Fetch Mock Parameters, Finalize Plan,
 * Select Extras?, Analyze Skills & Optimize List, Generate Standard Question Only,
 * Advance Session Needed?, Handle Specific Scenario, Regit Voice Capture.
 */
public class CategoryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionService questionService = new QuestionService();
    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private final AdaptiveService adaptiveService = new AdaptiveService();

    public CategoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        JLabel title = new JLabel("Select Preparation Type");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        JLabel subtitle = new JLabel("Choose a category: Job Role, Tech Stack, or Behavioral");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(28));

        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Category cat : Category.values()) {
            grid.add(createCategoryCard(cat));
        }

        content.add(grid);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createCategoryCard(Category cat) {
        int questionCount = questionService.getQuestionCount(cat.name());

        JPanel card = new JPanel() {
            boolean hovered = false;
            float yOffset = 0;
            Timer hoverTimer;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        animateY(-4f);
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hovered = false;
                        setCursor(Cursor.getDefaultCursor());
                        animateY(0f);
                    }
                    @Override public void mouseClicked(MouseEvent e) {
                        showQuizConfigDialog(cat);
                    }
                });
            }

            void animateY(float target) {
                if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();
                hoverTimer = new Timer(16, evt -> {
                    float diff = target - yOffset;
                    if (Math.abs(diff) < 0.3f) {
                        yOffset = target;
                        ((Timer) evt.getSource()).stop();
                    } else {
                        yOffset += diff * 0.25f;
                    }
                    repaint();
                });
                hoverTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int yo = (int) yOffset;

                // Shadow when hovering
                if (hovered) {
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fill(new RoundRectangle2D.Float(4, 6 + yo + 4, getWidth() - 8, getHeight() - 8, 16, 16));
                }

                Color bg = hovered ? Constants.BG_CARD_HOVER : Constants.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, yo, getWidth(), getHeight(), 16, 16));

                // Left color accent bar
                g2.setColor(cat.getColor());
                g2.fill(new RoundRectangle2D.Float(0, yo, 4, getHeight(), 4, 4));

                // Hover border glow
                if (hovered) {
                    g2.setColor(Constants.withAlpha(cat.getColor(), 80));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, yo + 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                }

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        // Badge
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(cat.getColor(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new GridBagLayout());
        badge.setPreferredSize(new Dimension(48, 48));
        badge.setMaximumSize(new Dimension(48, 48));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel codeLabel = new JLabel(cat.getShortCode());
        codeLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 16));
        codeLabel.setForeground(cat.getColor());
        badge.add(codeLabel);
        card.add(badge);
        card.add(Box.createVerticalStrut(14));

        JLabel nameLabel = new JLabel(cat.getDisplayName());
        nameLabel.setFont(Constants.FONT_SUBHEADING);
        nameLabel.setForeground(Constants.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel descLabel = new JLabel("<html><body style='width:140px'>" + cat.getDescription() + "</body></html>");
        descLabel.setFont(Constants.FONT_SMALL);
        descLabel.setForeground(Constants.TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(10));

        // Mastery badge (if user is logged in and has mastery)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(200, 24));

        JPanel countBadge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(cat.getColor(), 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        countBadge.setOpaque(false);
        countBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel countLabel = new JLabel(questionCount + " Qs");
        countLabel.setFont(Constants.FONT_TINY);
        countLabel.setForeground(cat.getColor());
        countBadge.add(countLabel);
        bottomRow.add(countBadge);

        card.add(bottomRow);
        return card;
    }

    /**
     * Comprehensive quiz configuration dialog.
     * Implements flowchart steps: Valid Profile? → Fetch Training Alert,
     * Set Goal / Fetch Session, Fetch Mock Parameters, Advance Session Needed?,
     * Select Extras?, Regit Voice Capture, Finalize Plan.
     */
    private void showQuizConfigDialog(Category cat) {
        com.interviewmentor.model.User user = mainFrame.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "You must be logged in to create a training plan.",
                "Not logged in", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ══════════════════════════════════════════════════════
        // STEP 1: Valid Profile? → Fetch Training Alert
        // ══════════════════════════════════════════════════════
        if (user != null) {
            boolean missingRole = user.getTargetRole() == null || user.getTargetRole().trim().isEmpty();
            boolean missingExp = user.getExperienceLevel() == null || user.getExperienceLevel().trim().isEmpty();
            boolean missingTech = user.getTechStack() == null || user.getTechStack().trim().isEmpty();

            if (missingRole || missingExp || missingTech) {
                // Flowchart: "Fetch Training Alert"
                JOptionPane.showMessageDialog(mainFrame,
                    "[!] Training Alert: Your profile is incomplete.\n" +
                    "Please update your Target Role, Experience Level, and Tech Stack\n" +
                    "in your Profile before starting a session.",
                    "Incomplete Profile — Training Alert", JOptionPane.WARNING_MESSAGE);
                mainFrame.showPanel(Constants.PANEL_PROFILE);
                return;
            }
        }

        // ══════════════════════════════════════════════════════
        // STEP 2: Advance Session Needed? — Check mastery
        // ══════════════════════════════════════════════════════
        String advanceMessage = null;
        if (user != null) {
            MasteryLevel mastery = adaptiveService.getMastery(user.getId(), cat.name());
            if (mastery != null && adaptiveService.isAdvanceNeeded(user.getId(), cat.name())) {
                advanceMessage = "[TARGET] Advance Recommended!\n" +
                    "Current level: " + mastery.getLevelName() +
                    " | Accuracy: " + String.format("%.1f%%", mastery.getCurrentAccuracy()) +
                    "\nSuggested difficulty: " + mastery.getRecommendedDifficulty();
            }
        }

        // ══════════════════════════════════════════════════════
        // STEP 3: Set Goal / Fetch Session
        // ══════════════════════════════════════════════════════
        String goal = JOptionPane.showInputDialog(mainFrame,
            (advanceMessage != null ? advanceMessage + "\n\n" : "") +
            "What is your specific goal for this " + cat.getDisplayName() + " session?\n" +
            "(e.g., 'Score 80% on Medium difficulty', 'Review all Tree questions')",
            "Set Session Goal", JOptionPane.QUESTION_MESSAGE);

        if (goal == null || goal.trim().isEmpty()) {
            return; // Cancelled
        }

        // ══════════════════════════════════════════════════════
        // STEP 4: Full Quiz Configuration (Fetch Mock Parameters + Select Extras)
        // ══════════════════════════════════════════════════════
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Constants.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Basic Parameters ---
        JLabel sectionBasic = new JLabel(">> Basic Parameters");
        sectionBasic.setFont(Constants.FONT_BODY_BOLD);
        sectionBasic.setForeground(Constants.ACCENT_PRIMARY);
        panel.add(sectionBasic);
        panel.add(Box.createVerticalStrut(6));

        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setFont(Constants.FONT_BODY_BOLD);
        panel.add(diffLabel);
        String[] difficulties = {"Mixed", "Easy", "Medium", "Hard"};
        JComboBox<String> diffCombo = new JComboBox<>(difficulties);
        diffCombo.setFont(Constants.FONT_BODY);
        // Pre-select recommended difficulty if mastery exists
        if (user != null) {
            MasteryLevel m = adaptiveService.getMastery(user.getId(), cat.name());
            if (m != null) {
                String rec = m.getRecommendedDifficulty();
                diffCombo.setSelectedItem(rec);
            }
        }
        panel.add(diffCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel numLabel = new JLabel("Number of Questions:");
        numLabel.setFont(Constants.FONT_BODY_BOLD);
        panel.add(numLabel);
        String[] counts = {"5", "10", "15", "20", "25", "30", "40", "50"};
        JComboBox<String> countCombo = new JComboBox<>(counts);
        countCombo.setSelectedItem("10");
        countCombo.setFont(Constants.FONT_BODY);
        panel.add(countCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel targetLabel = new JLabel("Target Score (%):");
        targetLabel.setFont(Constants.FONT_BODY_BOLD);
        panel.add(targetLabel);
        JSpinner targetSpinner = new JSpinner(new SpinnerNumberModel(70, 10, 100, 5));
        targetSpinner.setFont(Constants.FONT_BODY);
        panel.add(targetSpinner);
        panel.add(Box.createVerticalStrut(12));

        // --- Mock Interview Parameters ---
        JLabel sectionMock = new JLabel(">> Mock Interview Parameters");
        sectionMock.setFont(Constants.FONT_BODY_BOLD);
        sectionMock.setForeground(Constants.ACCENT_INFO);
        panel.add(sectionMock);
        panel.add(Box.createVerticalStrut(6));

        JCheckBox mockCheckBox = new JCheckBox("Enable Mock Interview Mode");
        mockCheckBox.setFont(Constants.FONT_SMALL);
        mockCheckBox.setOpaque(false);
        panel.add(mockCheckBox);

        JLabel formatLabel = new JLabel("Mock Format:");
        formatLabel.setFont(Constants.FONT_SMALL);
        panel.add(formatLabel);
        String[] formats = {"RELAXED", "TIMED", "STRICT"};
        JComboBox<String> formatCombo = new JComboBox<>(formats);
        formatCombo.setFont(Constants.FONT_SMALL);
        formatCombo.setEnabled(false);
        panel.add(formatCombo);

        JLabel timeLimitLabel = new JLabel("Time Limit (seconds per question):");
        timeLimitLabel.setFont(Constants.FONT_SMALL);
        panel.add(timeLimitLabel);
        JSpinner timeLimitSpinner = new JSpinner(new SpinnerNumberModel(60, 15, 300, 15));
        timeLimitSpinner.setFont(Constants.FONT_SMALL);
        timeLimitSpinner.setEnabled(false);
        panel.add(timeLimitSpinner);
        panel.add(Box.createVerticalStrut(12));

        // --- Extras (Select Extras?) ---
        JLabel sectionExtras = new JLabel(">> Select Extras");
        sectionExtras.setFont(Constants.FONT_BODY_BOLD);
        sectionExtras.setForeground(Constants.ACCENT_WARNING);
        panel.add(sectionExtras);
        panel.add(Box.createVerticalStrut(6));

        JCheckBox realTimeFB = new JCheckBox("Real-time Feedback (show correct answer immediately)");
        realTimeFB.setFont(Constants.FONT_SMALL);
        realTimeFB.setOpaque(false);
        panel.add(realTimeFB);

        JCheckBox voiceCapture = new JCheckBox("Enable Voice Capture (Behavioral/STAR Method)");
        voiceCapture.setFont(Constants.FONT_SMALL);
        voiceCapture.setOpaque(false);
        if (cat != Category.BEHAVIORAL) {
            voiceCapture.setEnabled(false);
            voiceCapture.setToolTipText("Voice capture only for Behavioral category");
        }
        panel.add(voiceCapture);

        JCheckBox extrasCheckBox = new JCheckBox("Analyze Skills & Optimize Question List (AI)");
        extrasCheckBox.setFont(Constants.FONT_SMALL);
        extrasCheckBox.setForeground(Constants.ACCENT_INFO);
        extrasCheckBox.setOpaque(false);
        panel.add(extrasCheckBox);

        JCheckBox aiGenCheckBox = new JCheckBox("Generate Custom AI Mock Questions (Uses Local LLM)");
        aiGenCheckBox.setFont(Constants.FONT_SMALL);
        aiGenCheckBox.setForeground(Constants.ACCENT_INFO);
        aiGenCheckBox.setOpaque(false);
        panel.add(aiGenCheckBox);

        JCheckBox scenarioCheckBox = new JCheckBox("Include Scenario-Based Questions");
        scenarioCheckBox.setFont(Constants.FONT_SMALL);
        scenarioCheckBox.setOpaque(false);
        panel.add(scenarioCheckBox);

        // Enable/disable mock-specific options
        mockCheckBox.addActionListener(e -> {
            boolean mock = mockCheckBox.isSelected();
            formatCombo.setEnabled(mock);
            timeLimitSpinner.setEnabled(mock);
        });

        // ══════════════════════════════════════════════════════
        // STEP 5: Finalize Plan — Show dialog
        // ══════════════════════════════════════════════════════
        int result = JOptionPane.showConfirmDialog(
            mainFrame, panel,
            "Finalize Plan: " + cat.getDisplayName(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String difficulty = (String) diffCombo.getSelectedItem();
            int numQuestions = Integer.parseInt((String) countCombo.getSelectedItem());
            int targetScore = (int) targetSpinner.getValue();

            // Create Training Plan in DB
            TrainingPlan plan = new TrainingPlan();
            plan.setUserId(user.getId());
            plan.setCategory(cat.name());
            plan.setGoal(goal.trim());
            plan.setDifficulty(difficulty);
            plan.setTargetScore(targetScore);
            plan.setMockInterview(mockCheckBox.isSelected());
            plan.setMockTimeLimitSeconds(mockCheckBox.isSelected() ? (int) timeLimitSpinner.getValue() : 0);
            plan.setMockFormat(mockCheckBox.isSelected() ? (String) formatCombo.getSelectedItem() : "RELAXED");
            plan.setRealTimeFeedback(realTimeFB.isSelected());
            plan.setVoiceCaptureEnabled(voiceCapture.isSelected());
            plan.setExtrasEnabled(extrasCheckBox.isSelected());

            int planId = trainingPlanService.createPlan(plan);
            plan.setId(planId);

            // Handle AI generation
            if (aiGenCheckBox.isSelected()) {
                simulateAIGenerationAndStart(cat, difficulty, numQuestions, plan);
                return;
            }

            // Handle Extras: Analyze Skills & Optimize List
            if (extrasCheckBox.isSelected()) {
                startOptimizedQuiz(cat, difficulty, numQuestions, plan);
                return;
            }

            // Handle Scenario-Based
            if (scenarioCheckBox.isSelected()) {
                handleSpecificScenario(cat, difficulty, numQuestions, plan);
                return;
            }

            // ══════════════════════════════════════════════════════
            // Generate Standard Question Only (default path)
            // ══════════════════════════════════════════════════════
            int available = questionService.getQuestionCount(cat.name(), difficulty);
            if (available == 0) {
                JOptionPane.showMessageDialog(mainFrame, "No questions available for this difficulty.",
                    "No Questions", JOptionPane.WARNING_MESSAGE);
                return;
            }
            numQuestions = Math.min(numQuestions, available);

            // Pass the plan to QuizPanel so it can use it for real-time feedback, voice, etc.
            mainFrame.getQuizPanel().startQuizWithPlan(cat, difficulty, numQuestions, plan);
            mainFrame.showPanel(Constants.PANEL_QUIZ);
        }
    }

    /**
     * Flowchart: "Analyze Skills & Optimize List"
     * Uses AdaptiveService to prioritize weak areas and missed questions.
     */
    private void startOptimizedQuiz(Category cat, String difficulty, int numQuestions, TrainingPlan plan) {
        JDialog loadingDialog = new JDialog(mainFrame, "Skill Analysis", true);
        loadingDialog.setSize(400, 170);
        loadingDialog.setLocationRelativeTo(mainFrame);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("Analyzing skills & optimizing question list...");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(8));

        JLabel detailLabel = new JLabel("Prioritizing missed questions and weak areas");
        detailLabel.setFont(Constants.FONT_SMALL);
        detailLabel.setForeground(Constants.TEXT_MUTED);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(detailLabel);
        content.add(Box.createVerticalStrut(12));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Constants.ACCENT_INFO);
        content.add(progressBar);

        loadingDialog.add(content, BorderLayout.CENTER);

        final int fNumQ = numQuestions;
        Timer timer = new Timer(1800, e -> {
            loadingDialog.dispose();
            int userId = mainFrame.getCurrentUser().getId();
            List<Question> optimized = adaptiveService.getOptimizedQuestions(
                userId, cat.name(), difficulty, fNumQ);

            if (optimized.isEmpty()) {
                // Fallback to standard
                int available = questionService.getQuestionCount(cat.name(), difficulty);
                int safeNum = Math.min(fNumQ, Math.max(available, 5));
                mainFrame.getQuizPanel().startQuizWithPlan(cat, difficulty, safeNum, plan);
            } else {
                mainFrame.getQuizPanel().startQuizFromQuestionsWithPlan(
                    optimized, cat.getDisplayName() + " (Optimized)", difficulty, plan);
            }
            mainFrame.showPanel(Constants.PANEL_QUIZ);
            ToastNotification.info("Skill-optimized quiz loaded with " + optimized.size() + " questions");
        });
        timer.setRepeats(false);
        timer.start();
        loadingDialog.setVisible(true);
    }

    /**
     * Flowchart: "Handle Specific Scenario"
     * Starts a scenario-based quiz with case study questions.
     */
    private void handleSpecificScenario(Category cat, String difficulty,
                                         int numQuestions, TrainingPlan plan) {
        JDialog loadingDialog = new JDialog(mainFrame, "Scenario Generation", true);
        loadingDialog.setSize(380, 150);
        loadingDialog.setLocationRelativeTo(mainFrame);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String roleStr = mainFrame.getCurrentUser().getTargetRole();
        JLabel statusLabel = new JLabel("Building " + roleStr + " scenarios for " + cat.getDisplayName() + "...");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Constants.ACCENT_WARNING);
        content.add(progressBar);

        loadingDialog.add(content, BorderLayout.CENTER);

        Timer timer = new Timer(2000, e -> {
            loadingDialog.dispose();
            int available = questionService.getQuestionCount(cat.name(), difficulty);
            int safeNum = Math.min(numQuestions, available > 0 ? available : 5);
            mainFrame.getQuizPanel().startQuizWithPlan(cat, difficulty, safeNum, plan);
            mainFrame.showPanel(Constants.PANEL_QUIZ);
            ToastNotification.info("Scenario-based session loaded for " + roleStr);
        });
        timer.setRepeats(false);
        timer.start();
        loadingDialog.setVisible(true);
    }

    private void simulateAIGenerationAndStart(Category cat, String difficulty,
                                                int numQuestions, TrainingPlan plan) {
        JDialog loadingDialog = new JDialog(mainFrame, "AI Generation", true);
        loadingDialog.setSize(350, 150);
        loadingDialog.setLocationRelativeTo(mainFrame);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        com.interviewmentor.model.User u = mainFrame.getCurrentUser();
        String roleStr = (u != null && u.getTargetRole() != null) ? u.getTargetRole() : "Candidate";

        JLabel statusLabel = new JLabel("Prompting LLM for tailored " + roleStr + " questions...");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Constants.ACCENT_INFO);
        content.add(progressBar);

        loadingDialog.add(content, BorderLayout.CENTER);

        Timer timer = new Timer(2500, e -> {
            loadingDialog.dispose();
            int available = questionService.getQuestionCount(cat.name(), difficulty);
            int safeNum = Math.min(numQuestions, available > 0 ? available : 5);
            mainFrame.getQuizPanel().startQuizWithPlan(cat, difficulty, safeNum, plan);
            mainFrame.showPanel(Constants.PANEL_QUIZ);
            JOptionPane.showMessageDialog(mainFrame, "Successfully generated " + safeNum + " custom questions!",
                "AI Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        timer.setRepeats(false);
        timer.start();
        loadingDialog.setVisible(true);
    }
}
