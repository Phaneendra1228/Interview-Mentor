package com.interviewmentor.view;

import com.interviewmentor.service.AchievementService;
import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Achievements panel showing earned and locked badges in a grid.
 */
public class AchievementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AchievementService achievementService = new AchievementService();
    private JPanel contentPanel;

    public AchievementPanel(MainFrame mainFrame) {
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

        int userId = mainFrame.getCurrentUser().getId();
        List<String> earned = achievementService.getEarnedAchievements(userId);

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Achievements");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        titleBox.add(title);
        JLabel subtitle = new JLabel(earned.size() + " of " + AchievementService.ACHIEVEMENTS.length + " unlocked");
        subtitle.setFont(Constants.FONT_BODY);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        titleBox.add(subtitle);
        headerRow.add(titleBox, BorderLayout.WEST);

        // Progress circle
        JPanel progressCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = Math.min(getWidth(), getHeight());
                float pct = (float) earned.size() / AchievementService.ACHIEVEMENTS.length;
                // Background arc
                g2.setColor(Constants.BG_CARD);
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(4, 4, s - 8, s - 8, 0, 360);
                // Progress arc
                g2.setColor(Constants.ACCENT_PRIMARY);
                g2.drawArc(4, 4, s - 8, s - 8, 90, -(int)(360 * pct));
                // Percentage text
                g2.setColor(Constants.TEXT_PRIMARY);
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = (int)(pct * 100) + "%";
                g2.drawString(text, (s - fm.stringWidth(text)) / 2, s / 2 + fm.getAscent() / 3);
                g2.dispose();
            }
        };
        progressCircle.setOpaque(false);
        progressCircle.setPreferredSize(new Dimension(56, 56));
        headerRow.add(progressCircle, BorderLayout.EAST);

        contentPanel.add(headerRow);
        contentPanel.add(Box.createVerticalStrut(28));

        // Achievement grid
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String[] achievement : AchievementService.ACHIEVEMENTS) {
            boolean unlocked = earned.contains(achievement[0]);
            grid.add(createBadgeCard(achievement, unlocked));
        }

        contentPanel.add(grid);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createBadgeCard(String[] achievement, boolean unlocked) {
        JPanel card = new JPanel() {
            boolean hov = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = unlocked
                    ? (hov ? Constants.withAlpha(Constants.ACCENT_PRIMARY, 30) : Constants.withAlpha(Constants.ACCENT_PRIMARY, 15))
                    : (hov ? Constants.BG_CARD_HOVER : Constants.BG_CARD);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                if (unlocked) {
                    g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 60));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                }

                // Lock overlay for locked achievements
                if (!unlocked) {
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        // Icon
        JLabel icon = new JLabel(achievement[3]);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, unlocked ? 36 : 28));
        icon.setForeground(unlocked ? Constants.TEXT_PRIMARY : Constants.TEXT_MUTED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(10));

        // Name
        JLabel name = new JLabel(achievement[1]);
        name.setFont(Constants.FONT_BODY_BOLD);
        name.setForeground(unlocked ? Constants.TEXT_PRIMARY : Constants.TEXT_MUTED);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(name);
        card.add(Box.createVerticalStrut(4));

        // Description
        JLabel desc = new JLabel("<html><center>" + achievement[2] + "</center></html>");
        desc.setFont(Constants.FONT_SMALL);
        desc.setForeground(unlocked ? Constants.TEXT_SECONDARY : Constants.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(desc);

        if (!unlocked) {
            card.add(Box.createVerticalStrut(6));
            JLabel lock = new JLabel("[LOCKED]");
            lock.setFont(Constants.FONT_TINY);
            lock.setForeground(Constants.TEXT_MUTED);
            lock.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lock);
        }

        return card;
    }
}
