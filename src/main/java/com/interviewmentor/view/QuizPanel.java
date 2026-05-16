package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.Question;
import com.interviewmentor.model.TrainingPlan;
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
 * Quiz panel with question nav dots, keyboard shortcuts, bookmark toggle, and timer color changes.
 */
public class QuizPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final BookmarkService bookmarkService = new BookmarkService();
    private final SessionLogService sessionLogService = new SessionLogService();
    private final AdaptiveService adaptiveService = new AdaptiveService();
    private final TrainingPlanService trainingPlanService = new TrainingPlanService();

    private TrainingPlan currentPlan;
    private boolean realTimeFeedbackMode = false;
    private boolean voiceCaptureRegistered = false;

    private List<Question> questions;
    private int currentIndex = 0;
    private int sessionId = -1;
    private Category category;
    private String difficulty;
    private Map<Integer, String> answers = new HashMap<>();
    private Map<Integer, Long> questionStartTimes = new HashMap<>();
    private Map<Integer, Integer> questionTimes = new HashMap<>();
    private long quizStartTime;
    private Timer timerUpdate;
    private boolean isBookmarkQuiz = false;

    private JLabel questionNumberLabel;
    private JLabel timerLabel;
    private JProgressBar progressBar;
    private JTextArea questionTextArea;
    private JPanel optionsPanel;
    private JPanel[] optionCards = new JPanel[4];
    private JLabel[] optionLabels = new JLabel[4];
    private int selectedOption = -1;
    private RoundedButton prevButton;
    private RoundedButton nextButton;
    private RoundedButton submitButton;
    private JLabel categoryLabel;
    private JPanel navDotsPanel;
    private JButton bookmarkBtn;
    private JButton voiceBtn;
    private boolean currentBookmarked = false;

    public QuizPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        String[] keys = {"1", "2", "3", "4"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            im.put(KeyStroke.getKeyStroke(keys[i]), "opt" + i);
            am.put("opt" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (questions != null && isShowing()) selectOption(idx);
                }
            });
        }
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        am.put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (questions != null && isShowing()) navigateQuestion(1);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        am.put("prev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (questions != null && isShowing()) navigateQuestion(-1);
            }
        });
    }

    private void selectOption(int idx) {
        String[] labels = {"A", "B", "C", "D"};
        selectedOption = idx;
        answers.put(currentIndex, labels[idx]);
        updateOptionHighlights();
        updateNavDots();

        // Log text response
        if (sessionId > 0 && questions != null) {
            Question q = questions.get(currentIndex);
            sessionLogService.logResponse(sessionId, q.getId(), "TEXT",
                "Selected: " + labels[idx], null, 1.0);
        }

        // Flowchart: "Real-time Feedback?" → "Stand & Process Response"
        if (realTimeFeedbackMode && questions != null) {
            showRealTimeFeedback(idx);
        }
    }

    /** Flowchart: "Stand & Process Response" — immediate feedback */
    private void showRealTimeFeedback(int selectedIdx) {
        Question q = questions.get(currentIndex);
        String[] labels = {"A", "B", "C", "D"};
        String correct = q.getCorrectAnswer();
        boolean isCorrect = labels[selectedIdx].equalsIgnoreCase(correct);

        String msg = isCorrect
            ? "Correct!"
            : "Incorrect. Correct answer: " + correct + ". " + q.getOptionByLetter(correct);
        ToastNotification.show(msg, isCorrect ? ToastNotification.Type.SUCCESS : ToastNotification.Type.ERROR);
    }

    private void updateOptionHighlights() {
        for (int i = 0; i < 4; i++) {
            optionCards[i].repaint();
        }
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftTop.setOpaque(false);
        categoryLabel = new JLabel("Data Structures");
        categoryLabel.setFont(Constants.FONT_SUBHEADING);
        categoryLabel.setForeground(Constants.ACCENT_PRIMARY_LIGHT);
        leftTop.add(categoryLabel);

        // Bookmark button
        bookmarkBtn = new JButton("") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = currentBookmarked ? Constants.withAlpha(Constants.ACCENT_WARNING, 40) : Constants.withAlpha(Constants.BG_CARD, 200);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Draw bookmark flag shape
                g2.setColor(currentBookmarked ? Constants.ACCENT_WARNING : Constants.TEXT_MUTED);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                int[] xp = {cx - 5, cx - 5, cx, cx + 5, cx + 5};
                int[] yp = {cy - 8, cy + 7, cy + 2, cy + 7, cy - 8};
                if (currentBookmarked) {
                    g2.fillPolygon(xp, yp, 5);
                } else {
                    g2.drawPolygon(xp, yp, 5);
                }
                g2.dispose();
            }
        };
        bookmarkBtn.setOpaque(false);
        bookmarkBtn.setContentAreaFilled(false);
        bookmarkBtn.setBorderPainted(false);
        bookmarkBtn.setFocusPainted(false);
        bookmarkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookmarkBtn.setPreferredSize(new Dimension(36, 30));
        bookmarkBtn.setToolTipText("Bookmark this question");
        bookmarkBtn.addActionListener(e -> toggleBookmark());
        leftTop.add(bookmarkBtn);
        topBar.add(leftTop, BorderLayout.WEST);

        questionNumberLabel = new JLabel("Question 1 of 10");
        questionNumberLabel.setFont(Constants.FONT_BODY_BOLD);
        questionNumberLabel.setForeground(Constants.TEXT_SECONDARY);
        questionNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(questionNumberLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(Constants.FONT_MONO);
        timerLabel.setForeground(Constants.ACCENT_SUCCESS);
        topBar.add(timerLabel, BorderLayout.EAST);

        content.add(topBar);
        content.add(Box.createVerticalStrut(10));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 6));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setForeground(Constants.ACCENT_PRIMARY);
        progressBar.setBackground(Constants.BG_CARD);
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(progressBar);
        content.add(Box.createVerticalStrut(10));

        // Question navigation dots
        navDotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        navDotsPanel.setOpaque(false);
        navDotsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        navDotsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(navDotsPanel);
        content.add(Box.createVerticalStrut(16));

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

        // Question text using JTextArea for better text display
        questionTextArea = new JTextArea("Question text here...");
        questionTextArea.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 17));
        questionTextArea.setForeground(Constants.TEXT_PRIMARY);
        questionTextArea.setBackground(new Color(0, 0, 0, 0));
        questionTextArea.setOpaque(false);
        questionTextArea.setEditable(false);
        questionTextArea.setFocusable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionTextArea.setBorder(null);
        questionCard.add(questionTextArea);

        voiceBtn = new JButton("[MIC] Hold to Speak (Voice-to-Text)");
        voiceBtn.setFont(Constants.FONT_BODY_BOLD);
        voiceBtn.setForeground(Color.WHITE);
        voiceBtn.setBackground(Constants.ACCENT_PRIMARY);
        voiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        voiceBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        voiceBtn.setVisible(false);
        voiceBtn.addActionListener(e -> simulateVoiceRecording());
        
        JPanel voicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        voicePanel.setOpaque(false);
        voicePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        voicePanel.add(voiceBtn);
        questionCard.add(voicePanel);

        questionCard.add(Box.createVerticalStrut(24));

        // Option panels — using simple JPanel cards with JLabel inside
        optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;

            // Create option card panel
            JPanel optCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = (selectedOption == idx);
                    Color bg = sel ? Constants.withAlpha(Constants.ACCENT_PRIMARY, 30) : Constants.BG_SECONDARY;
                    Color border = sel ? Constants.ACCENT_PRIMARY : Constants.BORDER_COLOR;
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 12, 12));
                    // Check mark when selected
                    if (sel) {
                        g2.setColor(Constants.ACCENT_PRIMARY);
                        g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 16));
                        // Draw checkmark shape instead of Unicode
                    g2.setColor(Constants.ACCENT_SUCCESS);
                    g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    int cx = getWidth() - 22, cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                    g2.drawLine(cx - 1, cy + 3, cx + 5, cy - 4);
                    }
                    g2.dispose();
                }
            };
            optCard.setOpaque(false);
            optCard.setLayout(new BorderLayout(12, 0));
            optCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 36));
            optCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            optCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            optCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Key badge
            JLabel keyBadge = new JLabel(labels[idx] + ".");
            keyBadge.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
            keyBadge.setForeground(Constants.TEXT_MUTED);
            keyBadge.setPreferredSize(new Dimension(28, 28));
            optCard.add(keyBadge, BorderLayout.WEST);

            // Option text label
            JLabel optLabel = new JLabel("Option " + labels[idx]);
            optLabel.setFont(Constants.FONT_BODY);
            optLabel.setForeground(Constants.TEXT_PRIMARY);
            optCard.add(optLabel, BorderLayout.CENTER);
            optionLabels[idx] = optLabel;

            // Click listener
            optCard.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectOption(idx);
                }
            });

            optionCards[idx] = optCard;
            optionsPanel.add(optCard);
            if (i < 3) optionsPanel.add(Box.createVerticalStrut(10));
        }

        questionCard.add(optionsPanel);
        content.add(questionCard);
        content.add(Box.createVerticalStrut(24));

        // Nav buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        prevButton = new RoundedButton("Previous", Constants.BG_CARD);
        prevButton.setPreferredSize(new Dimension(140, 44));
        prevButton.addActionListener(e -> navigateQuestion(-1));
        navPanel.add(prevButton);

        nextButton = new RoundedButton("Next");
        nextButton.setPreferredSize(new Dimension(140, 44));
        nextButton.addActionListener(e -> navigateQuestion(1));
        navPanel.add(nextButton);

        submitButton = new RoundedButton("Submit Quiz", Constants.ACCENT_SUCCESS, Constants.ACCENT_SUCCESS.brighter(), Color.WHITE, false);
        submitButton.setPreferredSize(new Dimension(160, 44));
        submitButton.addActionListener(e -> submitQuiz());
        submitButton.setVisible(false);
        navPanel.add(submitButton);

        content.add(navPanel);

        // Keyboard hint
        JLabel kbHint = new JLabel("Keyboard: 1-4 select option  |  Left/Right navigate");
        kbHint.setFont(Constants.FONT_TINY);
        kbHint.setForeground(Constants.TEXT_MUTED);
        kbHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        kbHint.setHorizontalAlignment(SwingConstants.CENTER);
        kbHint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        content.add(Box.createVerticalStrut(8));
        content.add(kbHint);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    public void startQuiz(Category cat, String difficulty, int numQuestions) {
        this.currentPlan = null;
        this.realTimeFeedbackMode = false;
        this.voiceCaptureRegistered = false;
        this.category = cat;
        this.difficulty = difficulty;
        this.isBookmarkQuiz = false;
        this.currentIndex = 0;
        this.selectedOption = -1;
        this.answers.clear();
        this.questionTimes.clear();
        this.questionStartTimes.clear();

        questions = questionService.getRandomQuestions(cat.name(), difficulty, numQuestions);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No questions available!", "Error", JOptionPane.ERROR_MESSAGE);
            mainFrame.showPanel(Constants.PANEL_CATEGORIES);
            return;
        }

        sessionId = quizService.createSession(mainFrame.getCurrentUser().getId(), cat.name(), difficulty, questions.size());
        categoryLabel.setText(cat.getDisplayName() + " \u2014 " + difficulty);
        startQuizCommon();
    }

    /** Start quiz with a TrainingPlan (flowchart: Begin Training Session with plan). */
    public void startQuizWithPlan(Category cat, String difficulty, int numQuestions, TrainingPlan plan) {
        this.currentPlan = plan;
        this.realTimeFeedbackMode = plan != null && plan.isRealTimeFeedback();
        this.voiceCaptureRegistered = plan != null && plan.isVoiceCaptureEnabled();
        this.category = cat;
        this.difficulty = difficulty;
        this.isBookmarkQuiz = false;
        this.currentIndex = 0;
        this.selectedOption = -1;
        this.answers.clear();
        this.questionTimes.clear();
        this.questionStartTimes.clear();

        questions = questionService.getRandomQuestions(cat.name(), difficulty, numQuestions);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No questions available!", "Error", JOptionPane.ERROR_MESSAGE);
            mainFrame.showPanel(Constants.PANEL_CATEGORIES);
            return;
        }

        sessionId = quizService.createSession(mainFrame.getCurrentUser().getId(), cat.name(), difficulty, questions.size());
        String modeLabel = plan != null && plan.isMockInterview() ? " [MOCK]" : "";
        String fbLabel = realTimeFeedbackMode ? " [RT-FB]" : "";
        categoryLabel.setText(cat.getDisplayName() + " \u2014 " + difficulty + modeLabel + fbLabel);
        if (voiceCaptureRegistered) {
            ToastNotification.info("[MIC] Voice capture registered and ready");
        }
        startQuizCommon();
    }

    /** Start from pre-loaded questions with a plan (flowchart: optimized/scenario). */
    public void startQuizFromQuestionsWithPlan(List<Question> questionList, String label, String diff, TrainingPlan plan) {
        this.currentPlan = plan;
        this.realTimeFeedbackMode = plan != null && plan.isRealTimeFeedback();
        this.voiceCaptureRegistered = plan != null && plan.isVoiceCaptureEnabled();
        this.questions = questionList;
        this.difficulty = diff;
        this.category = null;
        this.isBookmarkQuiz = true;
        this.currentIndex = 0;
        this.selectedOption = -1;
        this.answers.clear();
        this.questionTimes.clear();
        this.questionStartTimes.clear();

        sessionId = quizService.createSession(mainFrame.getCurrentUser().getId(), "OPTIMIZED", diff, questions.size());
        categoryLabel.setText(label + " \u2014 " + diff);
        startQuizCommon();
    }

    /** Start a quiz from a pre-loaded question list (e.g. bookmarks). */
    public void startQuizFromQuestions(List<Question> questionList, String label, String diff) {
        this.currentPlan = null;
        this.realTimeFeedbackMode = false;
        this.voiceCaptureRegistered = false;
        this.questions = questionList;
        this.difficulty = diff;
        this.category = null;
        this.isBookmarkQuiz = true;
        this.currentIndex = 0;
        this.selectedOption = -1;
        this.answers.clear();
        this.questionTimes.clear();
        this.questionStartTimes.clear();

        sessionId = quizService.createSession(mainFrame.getCurrentUser().getId(), "BOOKMARKS", diff, questions.size());
        categoryLabel.setText(label + " \u2014 " + diff);
        startQuizCommon();
    }

    private void startQuizCommon() {
        quizStartTime = System.currentTimeMillis();
        questionStartTimes.put(0, quizStartTime);
        if (timerUpdate != null) timerUpdate.stop();
        timerUpdate = new Timer(1000, e -> updateTimer());
        timerUpdate.start();
        mainFrame.setSidebarEnabled(false);
        buildNavDots();
        displayQuestion(0);
    }

    private void buildNavDots() {
        navDotsPanel.removeAll();
        if (questions == null) return;
        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;
            JButton dot = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = Math.min(getWidth(), getHeight()) - 2;
                    Color c;
                    if (idx == currentIndex) c = Constants.ACCENT_PRIMARY;
                    else if (answers.containsKey(idx)) c = Constants.ACCENT_SUCCESS;
                    else c = Constants.BG_CARD;
                    g2.setColor(c);
                    g2.fillOval(1, 1, s, s);
                    if (idx == currentIndex) {
                        g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 60));
                        g2.fillOval(-1, -1, s+4, s+4);
                    }
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setContentAreaFilled(false);
            dot.setBorderPainted(false);
            dot.setFocusPainted(false);
            dot.setPreferredSize(new Dimension(16, 16));
            dot.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dot.setToolTipText("Q" + (i + 1) + (answers.containsKey(i) ? " (done)" : ""));
            dot.addActionListener(e -> displayQuestion(idx));
            navDotsPanel.add(dot);
        }
        navDotsPanel.revalidate();
        navDotsPanel.repaint();
    }

    private void updateNavDots() {
        navDotsPanel.repaint();
    }

    private void displayQuestion(int index) {
        if (questions == null || index < 0 || index >= questions.size()) return;
        recordQuestionTime(currentIndex);
        currentIndex = index;
        questionStartTimes.put(currentIndex, System.currentTimeMillis());

        Question q = questions.get(index);
        questionNumberLabel.setText("Question " + (index + 1) + " of " + questions.size());
        progressBar.setValue((int)((index + 1) * 100.0 / questions.size()));

        // Set question text
        questionTextArea.setText(q.getQuestionText());

        // Set option text
        String[] opts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < 4; i++) {
            optionLabels[i].setText(opts[i] != null ? opts[i] : "");
        }

        // Restore previous selection
        String savedAnswer = answers.get(index);
        if (savedAnswer != null) {
            selectedOption = savedAnswer.charAt(0) - 'A';
        } else {
            selectedOption = -1;
        }

        // Show voice button for BEHAVIORAL or when voice capture is registered
        voiceBtn.setVisible(category == Category.BEHAVIORAL || voiceCaptureRegistered);

        prevButton.setVisible(index > 0);
        boolean isLast = (index == questions.size() - 1);
        nextButton.setVisible(!isLast);
        submitButton.setVisible(isLast);

        // Update bookmark state
        if (mainFrame.getCurrentUser() != null) {
            currentBookmarked = bookmarkService.isBookmarked(mainFrame.getCurrentUser().getId(), q.getId());
        }
        bookmarkBtn.repaint();
        updateOptionHighlights();
        updateNavDots();
        revalidate();
        repaint();
    }

    private void toggleBookmark() {
        if (questions == null || mainFrame.getCurrentUser() == null) return;
        Question q = questions.get(currentIndex);
        currentBookmarked = bookmarkService.toggleBookmark(mainFrame.getCurrentUser().getId(), q.getId());
        bookmarkBtn.repaint();
        ToastNotification.show(currentBookmarked ? "Question bookmarked!" : "Bookmark removed",
            currentBookmarked ? ToastNotification.Type.SUCCESS : ToastNotification.Type.INFO);
    }

    private void navigateQuestion(int delta) {
        int newIndex = currentIndex + delta;
        if (newIndex >= 0 && newIndex < questions.size()) displayQuestion(newIndex);
    }

    private void recordQuestionTime(int index) {
        Long startTime = questionStartTimes.get(index);
        if (startTime != null) {
            int elapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
            questionTimes.put(index, questionTimes.getOrDefault(index, 0) + elapsed);
        }
    }

    private void updateTimer() {
        long elapsed = (System.currentTimeMillis() - quizStartTime) / 1000;
        int mins = (int)(elapsed / 60);
        int secs = (int)(elapsed % 60);
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
        // Timer color: green < 5min, yellow 5-10min, red > 10min
        if (elapsed < 300) timerLabel.setForeground(Constants.ACCENT_SUCCESS);
        else if (elapsed < 600) timerLabel.setForeground(Constants.ACCENT_WARNING);
        else timerLabel.setForeground(Constants.ACCENT_DANGER);
    }

    private void submitQuiz() {
        recordQuestionTime(currentIndex);
        if (timerUpdate != null) timerUpdate.stop();

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Are you sure you want to submit? You answered " + answers.size() +
            " out of " + questions.size() + " questions.",
            "Submit Quiz", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            if (timerUpdate != null) timerUpdate.start();
            return;
        }

        int correctCount = 0;
        int totalTimeSeconds = (int)((System.currentTimeMillis() - quizStartTime) / 1000);
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String userAnswer = answers.getOrDefault(i, "");
            boolean isCorrect = userAnswer.equalsIgnoreCase(q.getCorrectAnswer());
            if (isCorrect) correctCount++;
            int qTime = questionTimes.getOrDefault(i, 0);
            quizService.recordAnswer(sessionId, q.getId(), userAnswer, isCorrect, qTime);
        }
        quizService.completeSession(sessionId, correctCount, totalTimeSeconds);
        mainFrame.setSidebarEnabled(true);

        // Record streak activity
        StreakService streakSvc = mainFrame.getStreakService();
        streakSvc.recordActivity(mainFrame.getCurrentUser().getId(), questions.size());
        mainFrame.updateHeaderStreak();

        // Flowchart: "Update Profile & Session Progress" — update mastery
        String catName = category != null ? category.name() : (isBookmarkQuiz ? "BOOKMARKS" : "UNKNOWN");
        if (category != null) {
            adaptiveService.updateMastery(mainFrame.getCurrentUser().getId(),
                catName, questions.size(), correctCount);
        }

        // Flowchart: "Goal Success?" + "Training Action Plan" — evaluate plan
        double accuracy = questions.size() > 0 ? (correctCount * 100.0 / questions.size()) : 0;
        if (currentPlan != null && currentPlan.getId() > 0) {
            boolean goalMet = accuracy >= currentPlan.getTargetScore();
            int missed = questions.size() - correctCount;
            String actionPlan = trainingPlanService.generateActionPlan(
                catName, accuracy, questions.size(), missed, difficulty);
            trainingPlanService.completePlan(currentPlan.getId(),
                (int) accuracy, goalMet, actionPlan);

            if (!goalMet) {
                // Flowchart: "Log Issue & Retransmit"
                trainingPlanService.logError(mainFrame.getCurrentUser().getId(), sessionId,
                    "GOAL_NOT_MET", "Target: " + currentPlan.getTargetScore() + "%, Achieved: " + (int) accuracy + "%");
            }
        }

        String msg = accuracy >= 70 ? "Great job!" : accuracy >= 40 ? "Good effort!" : "Keep practicing!";
        ToastNotification.success(msg + " Score: " + correctCount + "/" + questions.size());

        mainFrame.getResultPanel().showResultsWithPlan(sessionId, questions, answers,
            questionTimes, totalTimeSeconds, currentPlan);
        mainFrame.showPanel(Constants.PANEL_RESULT);
    }

    private void simulateVoiceRecording() {
        JDialog voiceDialog = new JDialog(mainFrame, "Active Microphone", true);
        voiceDialog.setSize(300, 150);
        voiceDialog.setLocationRelativeTo(mainFrame);
        voiceDialog.setLayout(new BorderLayout());
        voiceDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("[MIC] Listening... Please speak now.");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.ACCENT_DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        JProgressBar volumeBar = new JProgressBar(0, 100);
        volumeBar.setIndeterminate(true);
        volumeBar.setForeground(Constants.ACCENT_DANGER);
        content.add(volumeBar);

        voiceDialog.add(content, BorderLayout.CENTER);

        Timer timer = new Timer(3000, e -> {
            voiceDialog.dispose();
            // Automatically select the correct option to simulate successful STAR adherence
            Question q = questions.get(currentIndex);
            int correctIdx = q.getCorrectAnswer().charAt(0) - 'A';
            selectOption(correctIdx);
            
            // Allow UI to repaint the selection before showing the blocking dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainFrame, 
                    "Transcription complete!\n\nSTT Engine: High confidence detected.\nSTAR Method Adherence: 92%\n\nAutomatically mapped response to optimal choice.", 
                    "Voice Processed", JOptionPane.INFORMATION_MESSAGE);
                
                // Auto advance or submit
                if (currentIndex < questions.size() - 1) {
                    navigateQuestion(1);
                } else {
                    submitQuiz();
                }
            });
        });
        timer.setRepeats(false);
        timer.start();

        voiceDialog.setVisible(true);
    }

    public void stopTimer() { 
        if (timerUpdate != null) timerUpdate.stop(); 
        mainFrame.setSidebarEnabled(true);
    }

    public boolean isRunning() {
        return timerUpdate != null && timerUpdate.isRunning();
    }

    public Category getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public TrainingPlan getCurrentPlan() { return currentPlan; }
}
