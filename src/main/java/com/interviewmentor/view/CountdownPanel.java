package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.service.AnalyticsService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Interview Countdown Planner — set your interview date and get a personalized
 * study plan based on weak areas and days remaining.
 */
public class CountdownPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AnalyticsService analytics = new AnalyticsService();

    private LocalDate interviewDate;
    private JPanel contentPanel;

    public CountdownPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    public void refresh() {
        removeAll();
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Title
        JLabel title = new JLabel("Interview Countdown Planner");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(title);

        JLabel subtitle = new JLabel("Set your interview date to get a personalized study plan");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subtitle);
        contentPanel.add(Box.createVerticalStrut(24));

        // Date input row
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel dateLabel = new JLabel("Interview Date (YYYY-MM-DD):");
        dateLabel.setFont(Constants.FONT_BODY_BOLD);
        dateLabel.setForeground(Constants.TEXT_PRIMARY);
        dateRow.add(dateLabel);

        String defaultDate = interviewDate != null
                ? interviewDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE);
        JTextField dateField = new JTextField(defaultDate, 12);
        dateField.setFont(Constants.FONT_BODY);
        dateField.setPreferredSize(new Dimension(160, 36));
        dateRow.add(dateField);

        RoundedButton setBtn = new RoundedButton("Generate Plan");
        setBtn.setPreferredSize(new Dimension(160, 36));
        setBtn.addActionListener(e -> {
            try {
                interviewDate = LocalDate.parse(dateField.getText().trim());
                if (!interviewDate.isAfter(LocalDate.now())) {
                    com.interviewmentor.view.components.ToastNotification.error("Date must be in the future!");
                    return;
                }
                refresh();
            } catch (Exception ex) {
                com.interviewmentor.view.components.ToastNotification.error("Invalid date format. Use YYYY-MM-DD");
            }
        });
        dateRow.add(setBtn);

        contentPanel.add(dateRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // If interview date is set, show countdown + study plan
        if (interviewDate != null && interviewDate.isAfter(LocalDate.now())) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), interviewDate);
            buildCountdownDisplay(daysLeft);
            contentPanel.add(Box.createVerticalStrut(24));
            buildStudyPlan(daysLeft);
        }

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void buildCountdownDisplay(long daysLeft) {
        JPanel countdownCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Constants.withAlpha(Constants.ACCENT_PRIMARY, 30),
                        getWidth(), getHeight(), Constants.withAlpha(Constants.GRADIENT_END, 20));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        countdownCard.setOpaque(false);
        countdownCard.setLayout(new BoxLayout(countdownCard, BoxLayout.Y_AXIS));
        countdownCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        countdownCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        countdownCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Big countdown number
        JLabel countdownNum = new JLabel(String.valueOf(daysLeft));
        countdownNum.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 64));
        countdownNum.setForeground(daysLeft <= 3 ? Constants.ACCENT_DANGER :
                daysLeft <= 7 ? Constants.ACCENT_WARNING : Constants.ACCENT_PRIMARY);
        countdownNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        countdownCard.add(countdownNum);

        JLabel daysLabel = new JLabel("days until your interview on " +
                interviewDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        daysLabel.setFont(Constants.FONT_BODY);
        daysLabel.setForeground(Constants.TEXT_SECONDARY);
        daysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countdownCard.add(daysLabel);

        // Urgency message
        String urgency;
        Color urgencyColor;
        if (daysLeft <= 3) {
            urgency = "⚡ Final sprint! Focus only on your weakest areas.";
            urgencyColor = Constants.ACCENT_DANGER;
        } else if (daysLeft <= 7) {
            urgency = "⏰ One week left! Prioritize review and mock tests.";
            urgencyColor = Constants.ACCENT_WARNING;
        } else if (daysLeft <= 14) {
            urgency = "📚 Two weeks out. Great time for deep practice sessions.";
            urgencyColor = Constants.ACCENT_INFO;
        } else {
            urgency = "✅ Plenty of time. Build a solid foundation across all topics.";
            urgencyColor = Constants.ACCENT_SUCCESS;
        }
        countdownCard.add(Box.createVerticalStrut(8));
        JLabel urgencyLabel = new JLabel(urgency);
        urgencyLabel.setFont(Constants.FONT_BODY_BOLD);
        urgencyLabel.setForeground(urgencyColor);
        urgencyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countdownCard.add(urgencyLabel);

        contentPanel.add(countdownCard);
    }

    private void buildStudyPlan(long daysLeft) {
        int userId = mainFrame.getCurrentUser().getId();
        List<PerformanceStats> catStats = analytics.getCategoryStats(userId);

        JLabel planTitle = new JLabel("Your Personalized Study Plan");
        planTitle.setFont(Constants.FONT_HEADING);
        planTitle.setForeground(Constants.TEXT_PRIMARY);
        planTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(planTitle);
        contentPanel.add(Box.createVerticalStrut(16));

        // Sort categories: weakest first (lowest accuracy with attempts, then unattempted)
        catStats.sort((a, b) -> {
            if (a.getTotalAttempted() == 0 && b.getTotalAttempted() == 0) return 0;
            if (a.getTotalAttempted() == 0) return -1;
            if (b.getTotalAttempted() == 0) return 1;
            return Double.compare(a.getAccuracy(), b.getAccuracy());
        });

        int dailyCategories = daysLeft <= 7 ? 2 : 3;
        int dayIndex = 0;
        LocalDate currentDay = LocalDate.now().plusDays(1);

        JPanel planGrid = new JPanel(new GridLayout(0, 1, 0, 8));
        planGrid.setOpaque(false);
        planGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        planGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));

        for (int d = 0; d < Math.min(daysLeft, 14); d++) {
            LocalDate day = currentDay.plusDays(d);
            String dayStr = day.format(DateTimeFormatter.ofPattern("EEE, MMM d"));

            // Pick categories for this day
            StringBuilder topics = new StringBuilder();
            for (int c = 0; c < dailyCategories; c++) {
                int catIdx = (dayIndex + c) % catStats.size();
                PerformanceStats stat = catStats.get(catIdx);
                Category cat = Category.fromString(stat.getCategory());
                String catName = cat != null ? cat.getDisplayName() : stat.getCategory();
                if (c > 0) topics.append("  •  ");
                topics.append(catName);
                if (stat.getTotalAttempted() > 0) {
                    topics.append(" (").append(String.format("%.0f%%", stat.getAccuracy())).append(")");
                } else {
                    topics.append(" (New!)");
                }
            }
            dayIndex += dailyCategories;

            // Determine activity type based on remaining days
            String activity;
            if (d == (int) daysLeft - 1) {
                activity = "🧘 Light review & rest";
            } else if (daysLeft - d <= 3) {
                activity = "🎯 Mock Interview";
            } else if (d % 3 == 2) {
                activity = "📝 Practice Quiz (15 Qs)";
            } else {
                activity = "📖 Study & Flashcards";
            }

            planGrid.add(createDayCard(dayStr, topics.toString(), activity, d == 0));
        }

        if (daysLeft > 14) {
            JLabel moreLabel = new JLabel("... and " + (daysLeft - 14) + " more days. Plan updates as you progress!");
            moreLabel.setFont(Constants.FONT_SMALL);
            moreLabel.setForeground(Constants.TEXT_MUTED);
            moreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            planGrid.add(moreLabel);
        }

        contentPanel.add(planGrid);
    }

    private JPanel createDayCard(String day, String topics, String activity, boolean isToday) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isToday ? Constants.withAlpha(Constants.ACCENT_PRIMARY, 20) : Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (isToday) {
                    g2.setColor(Constants.ACCENT_PRIMARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setPreferredSize(new Dimension(110, 0));

        JLabel dayLabel = new JLabel(isToday ? "▶ TOMORROW" : day);
        dayLabel.setFont(Constants.FONT_BODY_BOLD);
        dayLabel.setForeground(isToday ? Constants.ACCENT_PRIMARY : Constants.TEXT_PRIMARY);
        leftCol.add(dayLabel);

        JLabel actLabel = new JLabel(activity);
        actLabel.setFont(Constants.FONT_SMALL);
        actLabel.setForeground(Constants.TEXT_MUTED);
        leftCol.add(actLabel);

        card.add(leftCol, BorderLayout.WEST);

        JLabel topicLabel = new JLabel(topics);
        topicLabel.setFont(Constants.FONT_BODY);
        topicLabel.setForeground(Constants.TEXT_SECONDARY);
        card.add(topicLabel, BorderLayout.CENTER);

        return card;
    }
}
