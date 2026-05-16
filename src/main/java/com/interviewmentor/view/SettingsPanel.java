package com.interviewmentor.view;

import com.interviewmentor.util.Constants;
import com.interviewmentor.service.DatabaseService;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Statement;

public class SettingsPanel extends JPanel {
    private final MainFrame mainFrame;

    public SettingsPanel(MainFrame mainFrame) {
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

        JLabel title = new JLabel("Settings & Preferences");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));



        // ─── Display & Preferences Section ───
        JPanel displayPanel = createSectionPanel("Display & Preferences");

        JCheckBox animationsBox = new JCheckBox("Enable UI Animations (Requires Restart)");
        animationsBox.setSelected(true);
        animationsBox.setFont(Constants.FONT_BODY);
        animationsBox.setForeground(Constants.TEXT_PRIMARY);
        animationsBox.setOpaque(false);
        displayPanel.add(animationsBox);
        displayPanel.add(Box.createVerticalStrut(10));

        JCheckBox soundsBox = new JCheckBox("Enable Sound Effects");
        soundsBox.setSelected(false);
        soundsBox.setFont(Constants.FONT_BODY);
        soundsBox.setForeground(Constants.TEXT_PRIMARY);
        soundsBox.setOpaque(false);
        displayPanel.add(soundsBox);

        content.add(displayPanel);
        content.add(Box.createVerticalStrut(20));

        // ─── Data Management Section ───
        JPanel dataPanel = createSectionPanel("Data Management");

        JLabel warningLabel = new JLabel("<html><body style='width: 500px; color: #94A3B8;'>"
                + "Resetting your progress will delete all quiz history, bookmarks, achievements, and activity streaks. "
                + "This action cannot be undone and will restart your interview preparation journey.</body></html>");
        warningLabel.setFont(Constants.FONT_BODY);
        dataPanel.add(warningLabel);
        dataPanel.add(Box.createVerticalStrut(16));

        RoundedButton resetBtn = new RoundedButton("Reset All Progress");
        resetBtn.setPreferredSize(new Dimension(220, 42));
        resetBtn.setMaximumSize(new Dimension(220, 42));
        resetBtn.setBackground(Constants.ACCENT_DANGER);
        resetBtn.addActionListener(e -> resetData());
        dataPanel.add(resetBtn);

        content.add(dataPanel);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String titleStr) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 0, 20, 0)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(titleStr);
        label.setFont(Constants.FONT_HEADING);
        label.setForeground(Constants.TEXT_PRIMARY);
        panel.add(label);
        panel.add(Box.createVerticalStrut(12));
        return panel;
    }

    private void resetData() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Are you absolutely sure you want to reset all your progress?\nThis action is permanent.",
            "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseService.getInstance().getConnection();
                 Statement s = c.createStatement()) {

                s.executeUpdate("DELETE FROM quiz_results");
                s.executeUpdate("DELETE FROM session_logs");
                s.executeUpdate("DELETE FROM quiz_sessions");
                s.executeUpdate("DELETE FROM daily_activity");
                s.executeUpdate("DELETE FROM bookmarks");
                s.executeUpdate("DELETE FROM achievements");
                s.executeUpdate("DELETE FROM mastery_tracking");
                s.executeUpdate("DELETE FROM spaced_repetition");
                s.executeUpdate("DELETE FROM training_plans");

                ToastNotification.success("Progress has been reset completely.");
                mainFrame.updateHeaderBadges();
                mainFrame.updateHeaderStreak();
            } catch (Exception ex) {
                ex.printStackTrace();
                ToastNotification.error("Failed to reset progress.");
            }
        }
    }
}
