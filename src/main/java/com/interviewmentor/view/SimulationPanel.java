package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.Question;
import com.interviewmentor.service.*;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interview Simulation Mode — strict timed mock interview.
 * No backtracking, countdown timer, pass/fail threshold.
 */
public class SimulationPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final AdaptiveService adaptiveService = new AdaptiveService();
    private final SpacedRepetitionService srService = new SpacedRepetitionService();

    private List<Question> questions;
    private int currentIndex = 0;
    private int sessionId = -1;
    private Category category;
    private Map<Integer, String> answers = new HashMap<>();
    private int correctCount = 0;
    private long quizStartTime;
    private int timeLimitSeconds = 0;
    private Timer countdownTimer;
    private boolean submitted = false;

    // UI
    private JPanel setupPanel;
    private JPanel quizPanel;
    private CardLayout cardLayout;
    private JLabel questionNumLabel, timerLabel, categoryLabel;
    private JTextArea questionTextArea;
    private JPanel optionsPanel;
    private JPanel[] optionCards = new JPanel[4];
    private JLabel[] optionLabels = new JLabel[4];
    private int selectedOption = -1;
    private JProgressBar progressBar;
    private RoundedButton confirmBtn;
    private JLabel statusLabel;

    public SimulationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        setupPanel = buildSetupPanel();
        quizPanel = buildQuizPanel();
        add(setupPanel, "setup");
        add(quizPanel, "quiz");
        cardLayout.show(this, "setup");
    }

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Top accent stripe
                g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_DANGER, getWidth(), 0, Constants.ACCENT_ORANGE));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(520, 560));

        // Title
        JLabel title = new JLabel("Interview Simulation");
        title.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(Constants.ACCENT_DANGER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel desc = new JLabel("<html><center>Strict timed mock interview. No going back.<br>Test yourself under real pressure.</center></html>");
        desc.setFont(Constants.FONT_BODY);
        desc.setForeground(Constants.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(28));

        // Category selection
        JLabel catLabel = new JLabel("Select Category");
        catLabel.setFont(Constants.FONT_BODY_BOLD);
        catLabel.setForeground(Constants.TEXT_PRIMARY);
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(catLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<String> catCombo = new JComboBox<>();
        for (Category c : Category.values()) catCombo.addItem(c.getDisplayName());
        catCombo.setFont(Constants.FONT_BODY);
        catCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        catCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(catCombo);
        card.add(Box.createVerticalStrut(18));

        // Difficulty
        JLabel diffLabel = new JLabel("Difficulty");
        diffLabel.setFont(Constants.FONT_BODY_BOLD);
        diffLabel.setForeground(Constants.TEXT_PRIMARY);
        diffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(diffLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Mixed"});
        diffCombo.setSelectedIndex(1);
        diffCombo.setFont(Constants.FONT_BODY);
        diffCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        diffCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(diffCombo);
        card.add(Box.createVerticalStrut(18));

        // Question count
        JLabel countLabel = new JLabel("Number of Questions");
        countLabel.setFont(Constants.FONT_BODY_BOLD);
        countLabel.setForeground(Constants.TEXT_PRIMARY);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(countLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<String> countCombo = new JComboBox<>(new String[]{"10", "15", "20", "25"});
        countCombo.setSelectedIndex(1);
        countCombo.setFont(Constants.FONT_BODY);
        countCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        countCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(countCombo);
        card.add(Box.createVerticalStrut(18));

        // Time limit
        JLabel timeLabel = new JLabel("Time Limit");
        timeLabel.setFont(Constants.FONT_BODY_BOLD);
        timeLabel.setForeground(Constants.TEXT_PRIMARY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(timeLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<String> timeCombo = new JComboBox<>(new String[]{"10 minutes", "15 minutes", "20 minutes", "30 minutes", "45 minutes"});
        timeCombo.setSelectedIndex(2);
        timeCombo.setFont(Constants.FONT_BODY);
        timeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        timeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(timeCombo);
        card.add(Box.createVerticalStrut(30));

        // Warning
        JLabel warn = new JLabel("<html><center>\u26a0 Once started, you cannot go back to previous questions.<br>The interview ends when time runs out or you finish all questions.</center></html>");
        warn.setFont(Constants.FONT_SMALL);
        warn.setForeground(Constants.ACCENT_WARNING);
        warn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(warn);
        card.add(Box.createVerticalStrut(20));

        // Start button
        RoundedButton startBtn = new RoundedButton("Begin Interview", Constants.ACCENT_DANGER, Constants.ACCENT_DANGER.brighter(), Color.WHITE, false);
        startBtn.setPreferredSize(new Dimension(240, 50));
        startBtn.setMaximumSize(new Dimension(240, 50));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> {
            Category cat = Category.values()[catCombo.getSelectedIndex()];
            String diff = (String) diffCombo.getSelectedItem();
            int numQ = Integer.parseInt((String) countCombo.getSelectedItem());
            String timeStr = (String) timeCombo.getSelectedItem();
            int mins = Integer.parseInt(timeStr.split(" ")[0]);
            startSimulation(cat, diff, numQ, mins * 60);
        });
        card.add(startBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(card, gbc);
        return panel;
    }

    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 36, 20, 36));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        categoryLabel = new JLabel("Interview Simulation");
        categoryLabel.setFont(Constants.FONT_SUBHEADING);
        categoryLabel.setForeground(Constants.ACCENT_DANGER);
        topBar.add(categoryLabel, BorderLayout.WEST);

        questionNumLabel = new JLabel("Q 1 / 15");
        questionNumLabel.setFont(Constants.FONT_BODY_BOLD);
        questionNumLabel.setForeground(Constants.TEXT_SECONDARY);
        questionNumLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(questionNumLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("20:00");
        timerLabel.setFont(Constants.FONT_MONO);
        timerLabel.setForeground(Constants.ACCENT_DANGER);
        topBar.add(timerLabel, BorderLayout.EAST);

        panel.add(topBar);
        panel.add(Box.createVerticalStrut(8));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 8));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        progressBar.setForeground(Constants.ACCENT_DANGER);
        progressBar.setBackground(Constants.BG_CARD);
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(6));

        // Status label
        statusLabel = new JLabel("\u26a0 No going back — choose carefully");
        statusLabel.setFont(Constants.FONT_SMALL);
        statusLabel.setForeground(Constants.ACCENT_WARNING);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(16));

        // Question card
        JPanel questionCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        questionCard.setOpaque(false);
        questionCard.setLayout(new BoxLayout(questionCard, BoxLayout.Y_AXIS));
        questionCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        questionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionTextArea = new JTextArea("Question text...");
        questionTextArea.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 17));
        questionTextArea.setForeground(Constants.TEXT_PRIMARY);
        questionTextArea.setOpaque(false);
        questionTextArea.setEditable(false);
        questionTextArea.setFocusable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionTextArea.setBorder(null);
        questionCard.add(questionTextArea);
        questionCard.add(Box.createVerticalStrut(24));

        optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JPanel optCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = (selectedOption == idx);
                    g2.setColor(sel ? Constants.withAlpha(Constants.ACCENT_DANGER, 30) : Constants.BG_SECONDARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(sel ? Constants.ACCENT_DANGER : Constants.BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 12, 12));
                    g2.dispose();
                }
            };
            optCard.setOpaque(false);
            optCard.setLayout(new BorderLayout(12, 0));
            optCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 36));
            optCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            optCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            optCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel keyBadge = new JLabel(labels[i] + ".");
            keyBadge.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
            keyBadge.setForeground(Constants.TEXT_MUTED);
            keyBadge.setPreferredSize(new Dimension(28, 28));
            optCard.add(keyBadge, BorderLayout.WEST);

            JLabel optLabel = new JLabel("Option " + labels[i]);
            optLabel.setFont(Constants.FONT_BODY);
            optLabel.setForeground(Constants.TEXT_PRIMARY);
            optCard.add(optLabel, BorderLayout.CENTER);
            optionLabels[idx] = optLabel;

            optCard.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (!submitted) { selectedOption = idx; updateOptionHighlights(); }
                }
            });
            optionCards[idx] = optCard;
            optionsPanel.add(optCard);
            if (i < 3) optionsPanel.add(Box.createVerticalStrut(10));
        }
        questionCard.add(optionsPanel);
        panel.add(questionCard);
        panel.add(Box.createVerticalStrut(20));

        // Confirm button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmBtn = new RoundedButton("Confirm & Next", Constants.ACCENT_DANGER, Constants.ACCENT_DANGER.brighter(), Color.WHITE, false);
        confirmBtn.setPreferredSize(new Dimension(200, 48));
        confirmBtn.addActionListener(e -> confirmAnswer());
        btnPanel.add(confirmBtn);

        panel.add(btnPanel);

        // Key bindings
        InputMap im = panel.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            im.put(KeyStroke.getKeyStroke(String.valueOf(i + 1)), "simOpt" + i);
            am.put("simOpt" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (questions != null && isShowing() && !submitted) { selectedOption = idx; updateOptionHighlights(); }
                }
            });
        }
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "simConfirm");
        am.put("simConfirm", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (questions != null && isShowing() && !submitted) confirmAnswer();
            }
        });

        return panel;
    }

    private void startSimulation(Category cat, String diff, int numQuestions, int timeLimit) {
        this.category = cat;
        this.timeLimitSeconds = timeLimit;
        this.currentIndex = 0;
        this.selectedOption = -1;
        this.correctCount = 0;
        this.submitted = false;
        this.answers.clear();

        questions = questionService.getRandomQuestions(cat.name(), diff, numQuestions);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No questions available for this category!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        sessionId = quizService.createSession(mainFrame.getCurrentUser().getId(), cat.name(), diff, questions.size());
        categoryLabel.setText("\u26a1 " + cat.getDisplayName() + " — " + diff + " [SIMULATION]");
        quizStartTime = System.currentTimeMillis();

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timer(1000, e -> updateCountdown());
        countdownTimer.start();
        mainFrame.setSidebarEnabled(false);

        displayQuestion(0);
        cardLayout.show(this, "quiz");
    }

    private void displayQuestion(int index) {
        if (questions == null || index < 0 || index >= questions.size()) return;
        currentIndex = index;
        selectedOption = -1;
        Question q = questions.get(index);

        questionNumLabel.setText("Q " + (index + 1) + " / " + questions.size());
        progressBar.setValue((int) ((index + 1) * 100.0 / questions.size()));
        questionTextArea.setText(q.getQuestionText());

        String[] opts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < 4; i++) optionLabels[i].setText(opts[i] != null ? opts[i] : "");

        boolean isLast = (index == questions.size() - 1);
        confirmBtn.setText(isLast ? "Submit Interview" : "Confirm & Next");

        updateOptionHighlights();
        revalidate();
        repaint();
    }

    private void confirmAnswer() {
        if (selectedOption < 0) {
            ToastNotification.show("Please select an answer!", ToastNotification.Type.WARNING);
            return;
        }

        String[] labels = {"A", "B", "C", "D"};
        String answer = labels[selectedOption];
        answers.put(currentIndex, answer);
        Question q = questions.get(currentIndex);
        boolean isCorrect = answer.equalsIgnoreCase(q.getCorrectAnswer());
        if (isCorrect) correctCount++;

        quizService.recordAnswer(sessionId, q.getId(), answer, isCorrect, 0);

        if (currentIndex < questions.size() - 1) {
            displayQuestion(currentIndex + 1);
        } else {
            finishSimulation();
        }
    }

    private void updateCountdown() {
        long elapsed = (System.currentTimeMillis() - quizStartTime) / 1000;
        int remaining = timeLimitSeconds - (int) elapsed;
        if (remaining <= 0) {
            // Time's up — auto-submit
            if (countdownTimer != null) countdownTimer.stop();
            ToastNotification.show("Time's up! Interview auto-submitted.", ToastNotification.Type.ERROR);
            finishSimulation();
            return;
        }
        int mins = remaining / 60, secs = remaining % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
        if (remaining <= 60) timerLabel.setForeground(Constants.ACCENT_DANGER);
        else if (remaining <= 180) timerLabel.setForeground(Constants.ACCENT_WARNING);
        else timerLabel.setForeground(Constants.ACCENT_SUCCESS);
    }

    private void finishSimulation() {
        submitted = true;
        if (countdownTimer != null) countdownTimer.stop();
        int totalTime = (int) ((System.currentTimeMillis() - quizStartTime) / 1000);
        quizService.completeSession(sessionId, correctCount, totalTime);
        mainFrame.setSidebarEnabled(true);

        // Record streak
        mainFrame.getStreakService().recordActivity(mainFrame.getCurrentUser().getId(), questions.size());
        mainFrame.updateHeaderStreak();

        // Update mastery
        if (category != null) {
            adaptiveService.updateMastery(mainFrame.getCurrentUser().getId(), category.name(), questions.size(), correctCount);
        }

        // Add missed to spaced repetition
        srService.addMissedFromSession(mainFrame.getCurrentUser().getId(), sessionId);

        // Show results via existing ResultPanel
        Map<Integer, Integer> qTimes = new HashMap<>();
        mainFrame.getResultPanel().showResults(sessionId, questions, answers, qTimes, totalTime);
        mainFrame.showPanel(Constants.PANEL_RESULT);
        cardLayout.show(this, "setup");
    }

    private void updateOptionHighlights() {
        for (int i = 0; i < 4; i++) optionCards[i].repaint();
    }

    public void stopTimer() { 
        if (countdownTimer != null) countdownTimer.stop(); 
        mainFrame.setSidebarEnabled(true);
    }

    public boolean isRunning() {
        return countdownTimer != null && countdownTimer.isRunning() && !submitted;
    }

    /** Reset to setup screen when navigated to */
    public void refresh() {
        cardLayout.show(this, "setup");
    }
}
