package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.Question;
import com.interviewmentor.service.QuestionService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Flashcard Mode — quick concept review with flippable cards.
 * Front = question, Back = answer + explanation. No scoring.
 */
public class FlashcardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionService questionService = new QuestionService();

    private List<Question> questions;
    private int currentIndex = 0;
    private boolean isFlipped = false;
    private float flipProgress = 0f;
    private Timer flipTimer;

    // Setup UI
    private CardLayout mainCardLayout;
    private JPanel setupPanel, cardPanel;

    // Card UI
    private JPanel flashcard;
    private JLabel indexLabel, hintLabel;
    private RoundedButton prevBtn, nextBtn, flipBtn, shuffleBtn;

    public FlashcardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        mainCardLayout = new CardLayout();
        setLayout(mainCardLayout);

        setupPanel = buildSetupPanel();
        cardPanel = buildCardPanel();
        add(setupPanel, "setup");
        add(cardPanel, "cards");
        mainCardLayout.show(this, "setup");
    }

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new GridBagLayout());

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_SECONDARY, getWidth(), 0, Constants.ACCENT_PRIMARY));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(480, 420));

        JLabel title = new JLabel("\ud83c\udccf Flashcard Mode");
        title.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(Constants.ACCENT_SECONDARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel desc = new JLabel("<html><center>Quick concept review. Flip cards to<br>reveal answers and explanations.</center></html>");
        desc.setFont(Constants.FONT_BODY);
        desc.setForeground(Constants.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(28));

        JLabel catLabel = new JLabel("Category");
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

        JLabel countLabel = new JLabel("Number of Cards");
        countLabel.setFont(Constants.FONT_BODY_BOLD);
        countLabel.setForeground(Constants.TEXT_PRIMARY);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(countLabel);
        card.add(Box.createVerticalStrut(6));

        JComboBox<String> countCombo = new JComboBox<>(new String[]{"10", "15", "20", "30", "50"});
        countCombo.setSelectedIndex(1);
        countCombo.setFont(Constants.FONT_BODY);
        countCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        countCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(countCombo);
        card.add(Box.createVerticalStrut(30));

        RoundedButton startBtn = new RoundedButton("Start Flashcards", Constants.ACCENT_SECONDARY, Constants.ACCENT_SECONDARY.brighter(), Color.WHITE, false);
        startBtn.setPreferredSize(new Dimension(220, 50));
        startBtn.setMaximumSize(new Dimension(220, 50));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> {
            Category cat = Category.values()[catCombo.getSelectedIndex()];
            int num = Integer.parseInt((String) countCombo.getSelectedItem());
            startFlashcards(cat, num);
        });
        card.add(startBtn);

        panel.add(card, new GridBagConstraints());
        return panel;
    }

    private JPanel buildCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_PRIMARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 60, 24, 60));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel modeLabel = new JLabel("\ud83c\udccf Flashcard Mode");
        modeLabel.setFont(Constants.FONT_SUBHEADING);
        modeLabel.setForeground(Constants.ACCENT_SECONDARY);
        topBar.add(modeLabel, BorderLayout.WEST);

        indexLabel = new JLabel("1 / 15");
        indexLabel.setFont(Constants.FONT_BODY_BOLD);
        indexLabel.setForeground(Constants.TEXT_SECONDARY);
        indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(indexLabel, BorderLayout.CENTER);

        RoundedButton backBtn = new RoundedButton("Back", Constants.BG_CARD);
        backBtn.setPreferredSize(new Dimension(80, 34));
        backBtn.addActionListener(e -> mainCardLayout.show(this, "setup"));
        topBar.add(backBtn, BorderLayout.EAST);

        panel.add(topBar);
        panel.add(Box.createVerticalStrut(24));

        // The flashcard
        flashcard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                // Scale for flip effect
                float scale = Math.abs(flipProgress - 0.5f) * 2f;
                int scaledW = (int)(w * scale);
                int xOff = (w - scaledW) / 2;

                boolean showBack = flipProgress > 0.5f;
                Color bgColor = showBack
                    ? new Color(0x162A4A)
                    : Constants.BG_CARD;
                Color accentColor = showBack ? Constants.ACCENT_SUCCESS : Constants.ACCENT_SECONDARY;

                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(xOff, 0, scaledW, h, 24, 24));
                g2.setColor(Constants.withAlpha(accentColor, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(xOff + 1, 1, scaledW - 2, h - 2, 24, 24));

                // Top accent
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(xOff, 0, scaledW, 4, 4, 4));

                if (scale > 0.1f && questions != null && currentIndex < questions.size()) {
                    Question q = questions.get(currentIndex);
                    g2.setClip(new RoundRectangle2D.Float(xOff + 10, 10, scaledW - 20, h - 20, 14, 14));

                    if (!showBack) {
                        // Front: Question
                        g2.setColor(Constants.TEXT_MUTED);
                        g2.setFont(Constants.FONT_SMALL);
                        g2.drawString("QUESTION", xOff + 30, 40);

                        g2.setColor(Constants.TEXT_PRIMARY);
                        g2.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 18));
                        drawWrappedText(g2, q.getQuestionText(), xOff + 30, 70, scaledW - 60);

                        g2.setColor(Constants.TEXT_MUTED);
                        g2.setFont(Constants.FONT_TINY);
                        g2.drawString("Click or press SPACE to flip", xOff + 30, h - 20);
                    } else {
                        // Back: Answer + Explanation
                        g2.setColor(Constants.ACCENT_SUCCESS);
                        g2.setFont(Constants.FONT_SMALL);
                        g2.drawString("ANSWER", xOff + 30, 40);

                        String correct = q.getCorrectAnswer();
                        String ansText = correct + ". " + q.getOptionByLetter(correct);
                        g2.setColor(Constants.TEXT_PRIMARY);
                        g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 18));
                        g2.drawString(ansText, xOff + 30, 68);

                        g2.setColor(Constants.TEXT_MUTED);
                        g2.setFont(Constants.FONT_SMALL);
                        g2.drawString("EXPLANATION", xOff + 30, 100);

                        g2.setColor(Constants.TEXT_SECONDARY);
                        g2.setFont(Constants.FONT_BODY);
                        String explanation = q.getExplanation() != null ? q.getExplanation() : "No explanation available.";
                        drawWrappedText(g2, explanation, xOff + 30, 120, scaledW - 60);
                    }
                }
                g2.dispose();
            }
        };
        flashcard.setOpaque(false);
        flashcard.setPreferredSize(new Dimension(0, 350));
        flashcard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        flashcard.setAlignmentX(Component.LEFT_ALIGNMENT);
        flashcard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        flashcard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { flipCard(); }
        });
        panel.add(flashcard);
        panel.add(Box.createVerticalStrut(8));

        hintLabel = new JLabel("Click the card or press SPACE to flip");
        hintLabel.setFont(Constants.FONT_SMALL);
        hintLabel.setForeground(Constants.TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(20));

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        prevBtn = new RoundedButton("\u2190 Previous", Constants.BG_CARD);
        prevBtn.setPreferredSize(new Dimension(140, 44));
        prevBtn.addActionListener(e -> navigate(-1));
        navPanel.add(prevBtn);

        flipBtn = new RoundedButton("Flip Card", Constants.ACCENT_SECONDARY, Constants.ACCENT_SECONDARY.brighter(), Color.WHITE, false);
        flipBtn.setPreferredSize(new Dimension(140, 44));
        flipBtn.addActionListener(e -> flipCard());
        navPanel.add(flipBtn);

        nextBtn = new RoundedButton("Next \u2192");
        nextBtn.setPreferredSize(new Dimension(140, 44));
        nextBtn.addActionListener(e -> navigate(1));
        navPanel.add(nextBtn);

        shuffleBtn = new RoundedButton("Shuffle", Constants.ACCENT_WARNING, Constants.ACCENT_WARNING.brighter(), Color.WHITE, false);
        shuffleBtn.setPreferredSize(new Dimension(120, 44));
        shuffleBtn.addActionListener(e -> shuffleCards());
        navPanel.add(shuffleBtn);

        panel.add(navPanel);

        // Key bindings
        InputMap im = panel.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "fcFlip");
        am.put("fcFlip", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (isShowing()) flipCard(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "fcPrev");
        am.put("fcPrev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (isShowing()) navigate(-1); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "fcNext");
        am.put("fcNext", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (isShowing()) navigate(1); }
        });

        return panel;
    }

    private void startFlashcards(Category cat, int count) {
        questions = questionService.getRandomQuestions(cat.name(), "Mixed", count);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No questions available!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentIndex = 0;
        isFlipped = false;
        flipProgress = 0f;
        updateDisplay();
        mainCardLayout.show(this, "cards");
    }

    private void navigate(int delta) {
        int newIndex = currentIndex + delta;
        if (newIndex >= 0 && newIndex < questions.size()) {
            currentIndex = newIndex;
            isFlipped = false;
            flipProgress = 0f;
            updateDisplay();
        }
    }

    private void flipCard() {
        if (flipTimer != null && flipTimer.isRunning()) return;
        boolean targetFlipped = !isFlipped;
        float end = targetFlipped ? 1f : 0f;

        flipTimer = new Timer(12, null);
        flipTimer.addActionListener(e -> {
            float diff = end - flipProgress;
            if (Math.abs(diff) < 0.03f) {
                flipProgress = end;
                isFlipped = targetFlipped;
                flipTimer.stop();
            } else {
                flipProgress += diff * 0.15f;
            }
            flashcard.repaint();
        });
        flipTimer.start();
    }

    private void shuffleCards() {
        if (questions == null) return;
        java.util.Collections.shuffle(questions);
        currentIndex = 0;
        isFlipped = false;
        flipProgress = 0f;
        updateDisplay();
    }

    private void updateDisplay() {
        if (questions == null) return;
        indexLabel.setText((currentIndex + 1) + " / " + questions.size());
        prevBtn.setEnabled(currentIndex > 0);
        nextBtn.setEnabled(currentIndex < questions.size() - 1);
        flashcard.repaint();
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        if (text == null) return;
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            if (fm.stringWidth(line + word) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString().trim(), x, lineY);
                line = new StringBuilder();
                lineY += fm.getHeight() + 2;
            }
            line.append(word).append(" ");
        }
        if (line.length() > 0) g2.drawString(line.toString().trim(), x, lineY);
    }

    public void refresh() {
        mainCardLayout.show(this, "setup");
    }
}
