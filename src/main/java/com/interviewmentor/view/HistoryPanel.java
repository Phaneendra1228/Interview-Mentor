package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.QuizSession;
import com.interviewmentor.service.QuizService;
import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * History panel with card-based rows, category/difficulty filtering, and search.
 */
public class HistoryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuizService quizService = new QuizService();
    private JPanel listPanel;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> difficultyFilter;
    private JLabel countLabel;

    public HistoryPanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("Quiz History");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        JLabel subtitle = new JLabel("Review your past quiz attempts");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);
        filterBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(Constants.FONT_BODY_BOLD);
        filterLabel.setForeground(Constants.TEXT_SECONDARY);
        filterBar.add(filterLabel);

        String[] categories = new String[Category.values().length + 1];
        categories[0] = "All Categories";
        for (int i = 0; i < Category.values().length; i++) {
            categories[i + 1] = Category.values()[i].getDisplayName();
        }
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.setFont(Constants.FONT_SMALL);
        categoryFilter.setPreferredSize(new Dimension(180, 30));
        categoryFilter.addActionListener(e -> refresh());
        filterBar.add(categoryFilter);

        String[] difficulties = {"All Difficulties", "Easy", "Medium", "Hard", "Mixed"};
        difficultyFilter = new JComboBox<>(difficulties);
        difficultyFilter.setFont(Constants.FONT_SMALL);
        difficultyFilter.setPreferredSize(new Dimension(150, 30));
        difficultyFilter.addActionListener(e -> refresh());
        filterBar.add(difficultyFilter);

        filterBar.add(Box.createHorizontalStrut(20));
        countLabel = new JLabel("0 results");
        countLabel.setFont(Constants.FONT_SMALL);
        countLabel.setForeground(Constants.TEXT_MUTED);
        filterBar.add(countLabel);

        content.add(filterBar);
        content.add(Box.createVerticalStrut(16));

        // Card header row
        JPanel headerRow = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Constants.BG_SIDEBAR);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        headerRow.setOpaque(false);
        headerRow.setLayout(new GridLayout(1, 6, 8, 0));
        headerRow.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] headers = {"Date", "Category", "Difficulty", "Score", "Accuracy", "Time"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h);
            lbl.setFont(Constants.FONT_BODY_BOLD);
            lbl.setForeground(Constants.TEXT_SECONDARY);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            headerRow.add(lbl);
        }
        content.add(headerRow);
        content.add(Box.createVerticalStrut(8));

        // List panel
        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(listPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        if (mainFrame.getCurrentUser() == null) return;

        listPanel.removeAll();
        List<QuizSession> sessions = quizService.getSessionsByUser(mainFrame.getCurrentUser().getId());

        // Apply filters
        String catFilter = (String) categoryFilter.getSelectedItem();
        String diffFilter = (String) difficultyFilter.getSelectedItem();

        List<QuizSession> filtered = sessions.stream()
            .filter(s -> {
                if (catFilter != null && !catFilter.equals("All Categories")) {
                    Category cat = Category.fromString(s.getCategory());
                    String catName = cat != null ? cat.getDisplayName() : s.getCategory();
                    return catName.equals(catFilter);
                }
                return true;
            })
            .filter(s -> {
                if (diffFilter != null && !diffFilter.equals("All Difficulties")) {
                    return diffFilter.equalsIgnoreCase(s.getDifficulty());
                }
                return true;
            })
            .collect(Collectors.toList());

        countLabel.setText(filtered.size() + " result" + (filtered.size() != 1 ? "s" : ""));

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No quiz history matches your filters");
            empty.setFont(Constants.FONT_BODY);
            empty.setForeground(Constants.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
            listPanel.add(empty);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");
            for (QuizSession s : filtered) {
                listPanel.add(createSessionCard(s, fmt));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createSessionCard(QuizSession s, DateTimeFormatter fmt) {
        Category cat = Category.fromString(s.getCategory());
        String catName = cat != null ? cat.getDisplayName() : s.getCategory();
        Color catColor = cat != null ? cat.getColor() : Constants.ACCENT_PRIMARY;
        double acc = s.getAccuracy();
        Color accColor = acc >= 70 ? Constants.ACCENT_SUCCESS : acc >= 40 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER;

        JPanel card = new JPanel() {
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Left accent
                g2.setColor(catColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 3, getHeight(), 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(1, 6, 8, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String dateStr = s.getStartedAt() != null ? s.getStartedAt().format(fmt) : "\u2014";
        addCenteredLabel(card, dateStr, Constants.TEXT_SECONDARY, Constants.FONT_SMALL);
        addCenteredLabel(card, catName, catColor, Constants.FONT_BODY_BOLD);
        addCenteredLabel(card, s.getDifficulty() != null ? s.getDifficulty() : "Mixed", Constants.TEXT_MUTED, Constants.FONT_SMALL);
        addCenteredLabel(card, s.getCorrectAnswers() + "/" + s.getTotalQuestions(), Constants.TEXT_PRIMARY, Constants.FONT_BODY_BOLD);
        addCenteredLabel(card, Constants.formatPercentage(acc), accColor, Constants.FONT_BODY_BOLD);
        addCenteredLabel(card, Constants.formatTime(s.getTimeTakenSeconds()), Constants.TEXT_SECONDARY, Constants.FONT_SMALL);

        return card;
    }

    private void addCenteredLabel(JPanel panel, String text, Color color, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl);
    }
}
