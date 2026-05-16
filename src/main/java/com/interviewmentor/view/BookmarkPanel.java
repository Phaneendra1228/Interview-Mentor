package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.Question;
import com.interviewmentor.service.BookmarkService;
import com.interviewmentor.service.QuestionService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Bookmarked questions panel: browse, review, and remove saved questions.
 */
public class BookmarkPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookmarkService bookmarkService = new BookmarkService();
    private final QuestionService questionService = new QuestionService();
    private JPanel contentPanel;
    private JPanel listPanel;

    public BookmarkPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    /** Refresh bookmark list when panel is shown. */
    public void refresh() {
        removeAll();

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        int userId = mainFrame.getCurrentUser().getId();

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Bookmarked Questions");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        titleBox.add(title);

        int count = bookmarkService.getBookmarkCount(userId);
        JLabel subtitle = new JLabel(count + " saved question" + (count != 1 ? "s" : ""));
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        titleBox.add(subtitle);
        headerRow.add(titleBox, BorderLayout.WEST);

        if (count > 0) {
            RoundedButton quizBtn = new RoundedButton("Quiz from Bookmarks");
            quizBtn.setPreferredSize(new Dimension(200, 44));
            quizBtn.addActionListener(e -> startBookmarkQuiz());
            headerRow.add(quizBtn, BorderLayout.EAST);
        }

        contentPanel.add(headerRow);
        contentPanel.add(Box.createVerticalStrut(20));

        // Bookmark list
        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Integer> questionIds = bookmarkService.getBookmarkedQuestionIds(userId);
        if (questionIds.isEmpty()) {
            JPanel emptyCard = createEmptyState();
            listPanel.add(emptyCard);
        } else {
            for (int qId : questionIds) {
                Question q = questionService.getQuestionById(qId);
                if (q != null) {
                    listPanel.add(createBookmarkCard(q, userId));
                    listPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        contentPanel.add(listPanel);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createBookmarkCard(Question q, int userId) {
        Category cat = Category.fromString(q.getCategory());
        Color catColor = cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY;
        String catName = cat != null ? cat.getDisplayName() : q.getCategory();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Left accent
                g2.setColor(catColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Left: question info
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // Category + difficulty badges
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLabel = new JLabel(catName);
        catLabel.setFont(Constants.FONT_SMALL);
        catLabel.setForeground(catColor);
        badges.add(catLabel);

        JLabel diffLabel = new JLabel(" · " + q.getDifficulty());
        diffLabel.setFont(Constants.FONT_SMALL);
        diffLabel.setForeground(Constants.TEXT_MUTED);
        badges.add(diffLabel);

        infoPanel.add(badges);
        infoPanel.add(Box.createVerticalStrut(6));

        // Question text
        JLabel qText = new JLabel("<html><body style='width:500px'>" + q.getQuestionText() + "</body></html>");
        qText.setFont(Constants.FONT_BODY);
        qText.setForeground(Constants.TEXT_PRIMARY);
        qText.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(qText);

        infoPanel.add(Box.createVerticalStrut(6));

        // Correct answer
        JLabel ansLabel = new JLabel("Answer: " + q.getCorrectAnswer() + ". " + q.getOptionByLetter(q.getCorrectAnswer()));
        ansLabel.setFont(Constants.FONT_SMALL);
        ansLabel.setForeground(Constants.ACCENT_SUCCESS);
        ansLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(ansLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // Right: remove button
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        JButton removeBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.withAlpha(Constants.ACCENT_DANGER, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Constants.ACCENT_DANGER);
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        removeBtn.setOpaque(false);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.setPreferredSize(new Dimension(36, 36));
        removeBtn.setMaximumSize(new Dimension(36, 36));
        removeBtn.setToolTipText("Remove bookmark");
        removeBtn.addActionListener(e -> {
            bookmarkService.removeBookmark(userId, q.getId());
            refresh();
        });
        actions.add(Box.createVerticalGlue());
        actions.add(removeBtn);
        actions.add(Box.createVerticalGlue());
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("⚑");
        icon.setFont(new Font("Dialog", Font.PLAIN, 48));
        icon.setForeground(Constants.TEXT_MUTED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(icon);

        panel.add(Box.createVerticalStrut(12));

        JLabel msg = new JLabel("No bookmarked questions yet");
        msg.setFont(Constants.FONT_HEADING);
        msg.setForeground(Constants.TEXT_SECONDARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(msg);

        panel.add(Box.createVerticalStrut(8));

        JLabel hint = new JLabel("Bookmark questions during quizzes to review them later");
        hint.setFont(Constants.FONT_BODY);
        hint.setForeground(Constants.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hint);

        return panel;
    }

    private void startBookmarkQuiz() {
        int userId = mainFrame.getCurrentUser().getId();
        List<Integer> ids = bookmarkService.getBookmarkedQuestionIds(userId);
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No bookmarked questions!", "Bookmarks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Question> questions = new ArrayList<>();
        for (int id : ids) {
            Question q = questionService.getQuestionById(id);
            if (q != null) questions.add(q);
        }
        if (questions.isEmpty()) return;

        // Limit to 20 questions max
        if (questions.size() > 20) {
            java.util.Collections.shuffle(questions);
            questions = questions.subList(0, 20);
        }

        mainFrame.getQuizPanel().startQuizFromQuestions(questions, "Bookmarks", "Mixed");
        mainFrame.showPanel(Constants.PANEL_QUIZ);
    }
}
