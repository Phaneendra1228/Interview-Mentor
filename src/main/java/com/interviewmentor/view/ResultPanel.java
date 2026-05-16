package com.interviewmentor.view;

import com.interviewmentor.model.Question;
import com.interviewmentor.model.TrainingPlan;
import com.interviewmentor.model.MasteryLevel;
import com.interviewmentor.service.*;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.CircularProgress;
import com.interviewmentor.view.components.ConfettiAnimation;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

/**
 * Quiz results with confetti celebration, achievement check, and bookmark toggle.
 */
public class ResultPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookmarkService bookmarkService = new BookmarkService();
    private final AdaptiveService adaptiveService = new AdaptiveService();
    private ConfettiAnimation confetti;

    public ResultPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    public void showResults(int sessionId, List<Question> questions,
                            Map<Integer, String> answers,
                            Map<Integer, Integer> questionTimes,
                            int totalTimeSeconds) {
        showResultsWithPlan(sessionId, questions, answers, questionTimes, totalTimeSeconds, null);
    }

    /**
     * Show results with training plan context.
     * Implements flowchart: Goal Success?, Post-Process Feedback, Improvement Possible?,
     * Generate Specific Recommendations, Training Action Plan, Check for Mastery & Plan Next,
     * Handle Missed Interview Questions, Return Results & Report to User.
     */
    public void showResultsWithPlan(int sessionId, List<Question> questions,
                            Map<Integer, String> answers,
                            Map<Integer, Integer> questionTimes,
                            int totalTimeSeconds, TrainingPlan plan) {
        removeAll();

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAns = answers.getOrDefault(i, "");
            if (userAns.equalsIgnoreCase(questions.get(i).getCorrectAnswer())) correct++;
        }
        double accuracy = questions.size() > 0 ? (correct * 100.0 / questions.size()) : 0;

        // Result message
        String resultMsg = accuracy >= 90 ? "* Outstanding!" :
                           accuracy >= 70 ? "* Great Job!" :
                           accuracy >= 50 ? "+ Good Effort!" :
                           accuracy >= 30 ? "# Keep Learning!" : "^ Don't Give Up!";

        JLabel header = new JLabel(resultMsg);
        header.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 30));
        header.setForeground(accuracy >= 70 ? Constants.ACCENT_SUCCESS : accuracy >= 50 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(header);
        contentPanel.add(Box.createVerticalStrut(4));
        JLabel subHeader = new JLabel("Quiz Results");
        subHeader.setFont(Constants.FONT_BODY);
        subHeader.setForeground(Constants.TEXT_SECONDARY);
        subHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subHeader);
        contentPanel.add(Box.createVerticalStrut(24));

        // Score summary card
        JPanel summaryCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        summaryCard.setOpaque(false);
        summaryCard.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));
        summaryCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        CircularProgress circle = new CircularProgress(160);
        Color accColor = accuracy >= 70 ? Constants.ACCENT_SUCCESS : accuracy >= 40 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER;
        circle.setProgressColor(accColor);
        circle.setLabel("Accuracy");
        circle.setStrokeWidth(12);
        circle.setPercentage(accuracy);
        summaryCard.add(circle);

        JPanel statsCol = new JPanel();
        statsCol.setOpaque(false);
        statsCol.setLayout(new BoxLayout(statsCol, BoxLayout.Y_AXIS));
        statsCol.add(createStatRow("Score", correct + " / " + questions.size(), accColor));
        statsCol.add(Box.createVerticalStrut(12));
        statsCol.add(createStatRow("Accuracy", Constants.formatPercentage(accuracy), accColor));
        statsCol.add(Box.createVerticalStrut(12));
        statsCol.add(createStatRow("Time", Constants.formatTime(totalTimeSeconds), Constants.TEXT_PRIMARY));
        statsCol.add(Box.createVerticalStrut(12));
        int avgTime = questions.size() > 0 ? totalTimeSeconds / questions.size() : 0;
        statsCol.add(createStatRow("Avg/Question", Constants.formatTime(avgTime), Constants.TEXT_PRIMARY));
        summaryCard.add(statsCol);

        contentPanel.add(summaryCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Generate Specific Recommendations / Training Action Plan (Flowchart step)
        if (accuracy < 100) {
            JPanel actionPlanCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_INFO, 15));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_INFO, 50));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
                    g2.dispose();
                }
            };
            actionPlanCard.setOpaque(false);
            actionPlanCard.setLayout(new BoxLayout(actionPlanCard, BoxLayout.Y_AXIS));
            actionPlanCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
            actionPlanCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel apTitle = new JLabel("Training Action Plan");
            apTitle.setFont(Constants.FONT_HEADING);
            apTitle.setForeground(Constants.ACCENT_INFO);
            actionPlanCard.add(apTitle);
            actionPlanCard.add(Box.createVerticalStrut(12));

            String recommendation = accuracy >= 80 ? "Minor gaps identified. Review the specific incorrect answers and focus on edge cases." :
                                    accuracy >= 50 ? "Improvement possible. Focus on strengthening core concepts before attempting advanced mock interviews." :
                                    "Significant knowledge gaps. We recommend reviewing fundamental study materials and retaking standard assessments.";

            JLabel recLabel = new JLabel("<html><body style='width: 600px; color: #E2E8F0;'>" + recommendation + "</body></html>");
            recLabel.setFont(Constants.FONT_BODY);
            actionPlanCard.add(recLabel);

            contentPanel.add(actionPlanCard);
            contentPanel.add(Box.createVerticalStrut(20));
        }

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        RoundedButton dashBtn = new RoundedButton("Back to Dashboard");
        dashBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_DASHBOARD));
        btnRow.add(dashBtn);
        RoundedButton retryBtn = new RoundedButton("Try Again", Constants.ACCENT_INFO);
        retryBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_CATEGORIES));
        btnRow.add(retryBtn);
        // Flowchart: "Post-Process Feedback" → "Performance Metrics"
        RoundedButton perfBtn = new RoundedButton("View Performance", Constants.ACCENT_SUCCESS);
        perfBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_PERFORMANCE));
        btnRow.add(perfBtn);
        // Flowchart: "Post-Process Feedback" → Resources
        RoundedButton resBtn = new RoundedButton("Study Resources", Constants.ACCENT_WARNING);
        resBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_RESOURCES));
        btnRow.add(resBtn);
        contentPanel.add(btnRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // ══════════════════════════════════════════════════════
        // Flowchart: "Goal Success?" — Show goal evaluation
        // ══════════════════════════════════════════════════════
        if (plan != null) {
            boolean goalMet = accuracy >= plan.getTargetScore();
            JPanel goalCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = goalMet ? Constants.withAlpha(Constants.ACCENT_SUCCESS, 15)
                                       : Constants.withAlpha(Constants.ACCENT_DANGER, 15);
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    Color border = goalMet ? Constants.withAlpha(Constants.ACCENT_SUCCESS, 50)
                                           : Constants.withAlpha(Constants.ACCENT_DANGER, 50);
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
                    g2.dispose();
                }
            };
            goalCard.setOpaque(false);
            goalCard.setLayout(new BoxLayout(goalCard, BoxLayout.Y_AXIS));
            goalCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            goalCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel goalTitle = new JLabel(goalMet ? "[OK] Goal Achieved!" : "[X] Goal Not Met");
            goalTitle.setFont(Constants.FONT_HEADING);
            goalTitle.setForeground(goalMet ? Constants.ACCENT_SUCCESS : Constants.ACCENT_DANGER);
            goalCard.add(goalTitle);
            goalCard.add(Box.createVerticalStrut(6));

            String goalText = "Goal: \"" + (plan.getGoal() != null ? plan.getGoal() : "N/A") + "\"";
            JLabel goalLabel = new JLabel(goalText);
            goalLabel.setFont(Constants.FONT_BODY);
            goalLabel.setForeground(Constants.TEXT_SECONDARY);
            goalCard.add(goalLabel);

            JLabel targetLabel = new JLabel("Target: " + plan.getTargetScore() + "%  |  Achieved: "
                + Constants.formatPercentage(accuracy));
            targetLabel.setFont(Constants.FONT_BODY_BOLD);
            targetLabel.setForeground(Constants.TEXT_PRIMARY);
            goalCard.add(targetLabel);

            contentPanel.add(goalCard);
            contentPanel.add(Box.createVerticalStrut(16));
        }

        // ══════════════════════════════════════════════════════
        // Flowchart: "Check for Mastery & Plan Next"
        // ══════════════════════════════════════════════════════
        int userId = mainFrame.getCurrentUser() != null ? mainFrame.getCurrentUser().getId() : -1;
        String catName = null;
        if (questions != null && !questions.isEmpty()) {
            catName = questions.get(0).getCategory();
        }
        if (userId > 0 && catName != null) {
            MasteryLevel mastery = adaptiveService.getMastery(userId, catName);
            if (mastery != null) {
                JPanel masteryCard = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 12));
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                        g2.dispose();
                    }
                };
                masteryCard.setOpaque(false);
                masteryCard.setLayout(new BoxLayout(masteryCard, BoxLayout.Y_AXIS));
                masteryCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
                masteryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel mTitle = new JLabel("Mastery Level: " + mastery.getLevelName()
                    + " (Level " + mastery.getLevel() + "/5)");
                mTitle.setFont(Constants.FONT_SUBHEADING);
                mTitle.setForeground(Constants.ACCENT_PRIMARY_LIGHT);
                masteryCard.add(mTitle);

                String nextStep = adaptiveService.getNextStepRecommendation(userId, catName);
                JLabel nextLabel = new JLabel(nextStep);
                nextLabel.setFont(Constants.FONT_BODY);
                nextLabel.setForeground(Constants.TEXT_SECONDARY);
                masteryCard.add(nextLabel);

                contentPanel.add(masteryCard);
                contentPanel.add(Box.createVerticalStrut(16));
            }
        }

        // ══════════════════════════════════════════════════════
        // Flowchart: "Handle Missed Interview Questions"
        // ══════════════════════════════════════════════════════
        int missedCount = questions.size() - correct;
        if (missedCount > 0 && userId > 0) {
            JPanel missedCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_WARNING, 12));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                    g2.dispose();
                }
            };
            missedCard.setOpaque(false);
            missedCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
            missedCard.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            missedCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            missedCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

            JLabel missedLabel = new JLabel("[!] " + missedCount + " missed question(s)");
            missedLabel.setFont(Constants.FONT_BODY_BOLD);
            missedLabel.setForeground(Constants.ACCENT_WARNING);
            missedCard.add(missedLabel);

            RoundedButton retryMissedBtn = new RoundedButton("Retry Missed Questions",
                Constants.ACCENT_WARNING);
            retryMissedBtn.addActionListener(e -> retryMissedQuestions(questions, answers));
            missedCard.add(retryMissedBtn);

            contentPanel.add(missedCard);
            contentPanel.add(Box.createVerticalStrut(16));
        }

        // Question Review (Flowchart: "Return Results & Report to User")
        JLabel reviewHeader = new JLabel("Question Review — Detailed Report");
        reviewHeader.setFont(Constants.FONT_HEADING);
        reviewHeader.setForeground(Constants.TEXT_PRIMARY);
        reviewHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(reviewHeader);
        contentPanel.add(Box.createVerticalStrut(12));

        // userId already declared above in mastery section
        for (int i = 0; i < questions.size(); i++) {
            contentPanel.add(createQuestionReview(i, questions.get(i), answers.getOrDefault(i, ""),
                questionTimes.getOrDefault(i, 0), userId));
            contentPanel.add(Box.createVerticalStrut(12));
        }

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);

        // Confetti overlay for good scores
        if (accuracy >= 70) {
            confetti = new ConfettiAnimation();
            JLayeredPane layered = mainFrame.getLayeredPane();
            confetti.setBounds(0, 0, layered.getWidth(), layered.getHeight());
            layered.add(confetti, JLayeredPane.POPUP_LAYER);
            confetti.start(accuracy >= 90 ? 200 : 100, () -> {
                layered.remove(confetti);
                layered.repaint();
            });
        }

        // Check achievements
        if (userId > 0) {
            AchievementService achSvc = mainFrame.getAchievementService();
            java.util.List<String[]> newBadges = achSvc.checkAndAward(userId);
            mainFrame.updateHeaderBadges();
            for (String[] badge : newBadges) {
                Timer delay = new Timer(1500, e -> {
                    ToastNotification.success("Achievement: " + badge[1] + " - " + badge[2]);
                });
                delay.setRepeats(false);
                delay.start();
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createStatRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(250, 30));
        JLabel lbl = new JLabel(label);
        lbl.setFont(Constants.FONT_BODY);
        lbl.setForeground(Constants.TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(Constants.FONT_BODY_BOLD);
        val.setForeground(valueColor);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel createQuestionReview(int index, Question q, String userAnswer, int timeSeconds, int userId) {
        boolean isCorrect = userAnswer.equalsIgnoreCase(q.getCorrectAnswer());
        boolean unanswered = userAnswer.isEmpty();
        boolean[] bookmarked = {userId > 0 && bookmarkService.isBookmarked(userId, q.getId())};

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                Color statusColor = unanswered ? Constants.TEXT_MUTED : (isCorrect ? Constants.ACCENT_SUCCESS : Constants.ACCENT_DANGER);
                g2.setColor(statusColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTop.setOpaque(false);
        JLabel numLabel = new JLabel("Q" + (index + 1));
        numLabel.setFont(Constants.FONT_BODY_BOLD);
        numLabel.setForeground(Constants.TEXT_PRIMARY);
        leftTop.add(numLabel);

        if (userId > 0) {
            JButton bmBtn = new JButton(bookmarked[0] ? "[BM]" : "[+]");
            bmBtn.setOpaque(false); bmBtn.setContentAreaFilled(false);
            bmBtn.setBorderPainted(false); bmBtn.setFocusPainted(false);
            bmBtn.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 10));
            bmBtn.setForeground(bookmarked[0] ? Constants.ACCENT_WARNING : Constants.TEXT_MUTED);
            bmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bmBtn.setPreferredSize(new Dimension(32, 24));
            bmBtn.addActionListener(e -> {
                bookmarked[0] = bookmarkService.toggleBookmark(userId, q.getId());
                bmBtn.setText(bookmarked[0] ? "[BM]" : "[+]");
                bmBtn.setForeground(bookmarked[0] ? Constants.ACCENT_WARNING : Constants.TEXT_MUTED);
            });
            leftTop.add(bmBtn);
        }
        topRow.add(leftTop, BorderLayout.WEST);

        String statusText = unanswered ? "Unanswered" : (isCorrect ? "[OK] Correct" : "[X] Incorrect");
        Color statusClr = unanswered ? Constants.TEXT_MUTED : (isCorrect ? Constants.ACCENT_SUCCESS : Constants.ACCENT_DANGER);
        JLabel statusLabel = new JLabel(statusText + "  |  " + Constants.formatTime(timeSeconds));
        statusLabel.setFont(Constants.FONT_SMALL);
        statusLabel.setForeground(statusClr);
        topRow.add(statusLabel, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createVerticalStrut(8));

        JLabel qText = new JLabel("<html><body style='width:600px'>" + q.getQuestionText() + "</body></html>");
        qText.setFont(Constants.FONT_BODY);
        qText.setForeground(Constants.TEXT_PRIMARY);
        qText.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(qText);
        card.add(Box.createVerticalStrut(8));

        if (!unanswered) {
            JLabel yourAns = new JLabel("Your answer: " + userAnswer + ". " + q.getOptionByLetter(userAnswer));
            yourAns.setFont(Constants.FONT_SMALL);
            yourAns.setForeground(isCorrect ? Constants.ACCENT_SUCCESS : Constants.ACCENT_DANGER);
            yourAns.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(yourAns);
        }
        if (!isCorrect) {
            JLabel correctAns = new JLabel("Correct: " + q.getCorrectAnswer() + ". " + q.getOptionByLetter(q.getCorrectAnswer()));
            correctAns.setFont(Constants.FONT_SMALL);
            correctAns.setForeground(Constants.ACCENT_SUCCESS);
            correctAns.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(correctAns);
        }

        if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
            card.add(Box.createVerticalStrut(8));
            JPanel explPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_INFO, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
            };
            explPanel.setOpaque(false);
            explPanel.setLayout(new BorderLayout());
            explPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            explPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel explLabel = new JLabel("<html><body style='width:580px'><b>Explanation:</b> " + q.getExplanation() + "</body></html>");
            explLabel.setFont(Constants.FONT_SMALL);
            explLabel.setForeground(Constants.TEXT_SECONDARY);
            explPanel.add(explLabel);
            card.add(explPanel);
        }

        return card;
    }

    /** Flowchart: "Handle Missed Interview Questions" — retry only wrong answers */
    private void retryMissedQuestions(List<Question> questions, Map<Integer, String> answers) {
        java.util.List<Question> missed = new java.util.ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String userAns = answers.getOrDefault(i, "");
            if (!userAns.equalsIgnoreCase(questions.get(i).getCorrectAnswer())) {
                missed.add(questions.get(i));
            }
        }
        if (missed.isEmpty()) {
            ToastNotification.info("No missed questions to retry!");
            return;
        }
        mainFrame.getQuizPanel().startQuizFromQuestions(missed, "Missed Questions Review", "Mixed");
        mainFrame.showPanel(Constants.PANEL_QUIZ);
    }
}
