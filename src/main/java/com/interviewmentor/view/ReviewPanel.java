package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.Question;
import com.interviewmentor.model.User;
import com.interviewmentor.service.SpacedRepetitionService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Spaced Repetition Daily Review panel.
 * Shows questions due for review today using the SM-2 algorithm.
 */
public class ReviewPanel extends JPanel {

    private final MainFrame mainFrame;
    private final SpacedRepetitionService srService = new SpacedRepetitionService();

    private List<Question> dueQuestions;
    private int currentIndex = 0;
    private boolean answerRevealed = false;

    private CardLayout mainCardLayout;
    private JPanel emptyPanel, reviewPanel;

    // Review UI
    private JLabel indexLabel, categoryLabel;
    private JTextArea questionTextArea;
    private JPanel answerPanel;
    private JLabel answerLabel, explanationLabel;
    private RoundedButton revealBtn;
    private JPanel ratingPanel;
    private JLabel statsLabel;

    public ReviewPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        mainCardLayout = new CardLayout();
        setLayout(mainCardLayout);

        emptyPanel = buildEmptyPanel();
        reviewPanel = buildReviewPanel();
        add(emptyPanel, "empty");
        add(reviewPanel, "review");
    }

    private JPanel buildEmptyPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new GridBagLayout());

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 300));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel icon = new JLabel("\u2705");
        icon.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("All Caught Up!");
        title.setFont(Constants.FONT_HEADING);
        title.setForeground(Constants.ACCENT_SUCCESS);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("<html><center>No questions due for review today.<br>Take more quizzes to build your review queue.</center></html>");
        desc.setFont(Constants.FONT_BODY);
        desc.setForeground(Constants.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(20));

        statsLabel = new JLabel("Tracked: 0 | Mastered: 0");
        statsLabel.setFont(Constants.FONT_SMALL);
        statsLabel.setForeground(Constants.TEXT_MUTED);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statsLabel);
        card.add(Box.createVerticalStrut(20));

        RoundedButton quizBtn = new RoundedButton("Take a Quiz");
        quizBtn.setPreferredSize(new Dimension(160, 44));
        quizBtn.setMaximumSize(new Dimension(160, 44));
        quizBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizBtn.addActionListener(e -> mainFrame.showPanel(Constants.PANEL_CATEGORIES));
        card.add(quizBtn);

        panel.add(card, new GridBagConstraints());
        return panel;
    }

    private JPanel buildReviewPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel modeLabel = new JLabel("\ud83d\udd04 Spaced Repetition Review");
        modeLabel.setFont(Constants.FONT_SUBHEADING);
        modeLabel.setForeground(Constants.ACCENT_INFO);
        topBar.add(modeLabel, BorderLayout.WEST);

        indexLabel = new JLabel("1 / 5");
        indexLabel.setFont(Constants.FONT_BODY_BOLD);
        indexLabel.setForeground(Constants.TEXT_SECONDARY);
        indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(indexLabel, BorderLayout.CENTER);

        categoryLabel = new JLabel("");
        categoryLabel.setFont(Constants.FONT_SMALL);
        categoryLabel.setForeground(Constants.TEXT_MUTED);
        topBar.add(categoryLabel, BorderLayout.EAST);

        panel.add(topBar);
        panel.add(Box.createVerticalStrut(20));

        // Question card
        JPanel questionCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Constants.ACCENT_INFO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        questionCard.setOpaque(false);
        questionCard.setLayout(new BoxLayout(questionCard, BoxLayout.Y_AXIS));
        questionCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        questionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionTextArea = new JTextArea("Question...");
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
        questionCard.add(Box.createVerticalStrut(16));

        // Options (display all 4)
        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            JLabel optLbl = new JLabel(labels[i] + ". ...");
            optLbl.setFont(Constants.FONT_BODY);
            optLbl.setForeground(Constants.TEXT_SECONDARY);
            optLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            optLbl.setName("opt" + i);
            questionCard.add(optLbl);
            questionCard.add(Box.createVerticalStrut(6));
        }

        panel.add(questionCard);
        panel.add(Box.createVerticalStrut(16));

        // Reveal button
        revealBtn = new RoundedButton("Reveal Answer", Constants.ACCENT_INFO, Constants.ACCENT_INFO.brighter(), Color.WHITE, false);
        revealBtn.setPreferredSize(new Dimension(200, 48));
        revealBtn.setMaximumSize(new Dimension(200, 48));
        revealBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        revealBtn.addActionListener(e -> revealAnswer());
        panel.add(revealBtn);
        panel.add(Box.createVerticalStrut(12));

        // Answer panel (initially hidden)
        answerPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(Constants.ACCENT_SUCCESS, 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Constants.withAlpha(Constants.ACCENT_SUCCESS, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        answerPanel.setOpaque(false);
        answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.Y_AXIS));
        answerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        answerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerPanel.setVisible(false);

        answerLabel = new JLabel("Correct Answer: A");
        answerLabel.setFont(Constants.FONT_BODY_BOLD);
        answerLabel.setForeground(Constants.ACCENT_SUCCESS);
        answerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerPanel.add(answerLabel);
        answerPanel.add(Box.createVerticalStrut(6));

        explanationLabel = new JLabel("Explanation...");
        explanationLabel.setFont(Constants.FONT_BODY);
        explanationLabel.setForeground(Constants.TEXT_SECONDARY);
        explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerPanel.add(explanationLabel);

        panel.add(answerPanel);
        panel.add(Box.createVerticalStrut(16));

        // Rating panel
        ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        ratingPanel.setOpaque(false);
        ratingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingPanel.setVisible(false);

        JLabel rateLabel = new JLabel("How well did you know this?");
        rateLabel.setFont(Constants.FONT_SMALL);
        rateLabel.setForeground(Constants.TEXT_MUTED);
        ratingPanel.add(rateLabel);

        String[][] ratings = {
            {"Forgot", "1"}, {"Hard", "3"}, {"Good", "4"}, {"Easy", "5"}
        };
        Color[] ratingColors = {Constants.ACCENT_DANGER, Constants.ACCENT_WARNING, Constants.ACCENT_INFO, Constants.ACCENT_SUCCESS};
        for (int i = 0; i < ratings.length; i++) {
            RoundedButton rb = new RoundedButton(ratings[i][0], ratingColors[i], ratingColors[i].brighter(), Color.WHITE, false);
            rb.setPreferredSize(new Dimension(90, 40));
            int quality = Integer.parseInt(ratings[i][1]);
            rb.addActionListener(e -> rateAndNext(quality));
            ratingPanel.add(rb);
        }
        panel.add(ratingPanel);

        // Key bindings
        InputMap im = panel.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "revealSR");
        am.put("revealSR", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (isShowing() && !answerRevealed) revealAnswer(); }
        });

        return panel;
    }

    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user == null) { mainCardLayout.show(this, "empty"); return; }

        int tracked = srService.getTotalTracked(user.getId());
        int mastered = srService.getMasteredCount(user.getId());
        statsLabel.setText("Tracked: " + tracked + " | Mastered: " + mastered);

        dueQuestions = srService.getDueQuestions(user.getId(), 30);
        if (dueQuestions.isEmpty()) {
            mainCardLayout.show(this, "empty");
        } else {
            currentIndex = 0;
            answerRevealed = false;
            displayCurrentQuestion();
            mainCardLayout.show(this, "review");
        }
    }

    private void displayCurrentQuestion() {
        if (dueQuestions == null || currentIndex >= dueQuestions.size()) {
            ToastNotification.success("Review session complete! Great job!");
            mainCardLayout.show(this, "empty");
            refresh();
            return;
        }

        Question q = dueQuestions.get(currentIndex);
        indexLabel.setText((currentIndex + 1) + " / " + dueQuestions.size());

        Category cat = Category.fromString(q.getCategory());
        categoryLabel.setText(cat != null ? cat.getDisplayName() : q.getCategory());

        questionTextArea.setText(q.getQuestionText());

        // Update option labels
        String[] opts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        String[] letters = {"A", "B", "C", "D"};
        Component[] comps = ((JPanel) questionTextArea.getParent()).getComponents();
        int optIdx = 0;
        for (Component c : comps) {
            if (c instanceof JLabel && c.getName() != null && c.getName().startsWith("opt")) {
                ((JLabel) c).setText(letters[optIdx] + ". " + (opts[optIdx] != null ? opts[optIdx] : ""));
                optIdx++;
                if (optIdx >= 4) break;
            }
        }

        answerRevealed = false;
        answerPanel.setVisible(false);
        ratingPanel.setVisible(false);
        revealBtn.setVisible(true);
        revalidate();
        repaint();
    }

    private void revealAnswer() {
        if (dueQuestions == null || currentIndex >= dueQuestions.size()) return;
        Question q = dueQuestions.get(currentIndex);
        String correct = q.getCorrectAnswer();
        answerLabel.setText("Correct Answer: " + correct + ". " + q.getOptionByLetter(correct));
        String expl = q.getExplanation() != null ? q.getExplanation() : "No explanation available.";
        if (expl.length() > 120) expl = expl.substring(0, 117) + "...";
        explanationLabel.setText(expl);

        answerRevealed = true;
        answerPanel.setVisible(true);
        ratingPanel.setVisible(true);
        revealBtn.setVisible(false);
        revalidate();
        repaint();
    }

    private void rateAndNext(int quality) {
        if (dueQuestions == null || currentIndex >= dueQuestions.size()) return;
        User user = mainFrame.getCurrentUser();
        if (user == null) return;

        Question q = dueQuestions.get(currentIndex);
        srService.recordReview(user.getId(), q.getId(), quality);

        currentIndex++;
        displayCurrentQuestion();
    }
}
