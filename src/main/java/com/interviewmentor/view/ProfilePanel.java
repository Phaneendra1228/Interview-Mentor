package com.interviewmentor.view;

import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.model.User;
import com.interviewmentor.service.*;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.CircularProgress;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.io.File;

/**
 * User profile panel with stats overview, account info, and achievement summary.
 */
public class ProfilePanel extends JPanel {

    private final MainFrame mainFrame;
    private final AnalyticsService analytics = new AnalyticsService();
    private final StreakService streakService = new StreakService();
    private final AchievementService achievementService = new AchievementService();
    private final BookmarkService bookmarkService = new BookmarkService();
    private final PdfExportService pdfExportService = new PdfExportService();

    public ProfilePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
    }

    public void refresh() {
        removeAll();
        User user = mainFrame.getCurrentUser();
        if (user == null) return;

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        // Profile header card
        JPanel profileCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, Constants.withAlpha(Constants.ACCENT_PRIMARY, 25),
                    getWidth(), getHeight(), Constants.withAlpha(Constants.GRADIENT_END, 25));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        profileCard.setOpaque(false);
        profileCard.setLayout(new FlowLayout(FlowLayout.LEFT, 28, 24));
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JPanel avatar = new JPanel() {
            private java.awt.Image avatarImg;
            {
                try {
                    // Try external first
                    java.io.File extFile = new java.io.File("user_avatar.png");
                    if (extFile.exists()) {
                        avatarImg = javax.imageio.ImageIO.read(extFile);
                    } else {
                        // Fallback to resources
                        java.io.InputStream is = getClass().getResourceAsStream("/images/avatar.png");
                        if (is != null) {
                            avatarImg = javax.imageio.ImageIO.read(is);
                        }
                    }
                } catch (Exception e) {}
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = Math.min(getWidth(), getHeight());
                
                java.awt.geom.Ellipse2D.Double clip = new java.awt.geom.Ellipse2D.Double(0, 0, s, s);
                g2.setClip(clip);

                if (avatarImg != null) {
                    g2.drawImage(avatarImg, 0, 0, s, s, null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_PRIMARY, s, s, Constants.GRADIENT_END));
                    g2.fillOval(0, 0, s, s);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 36));
                    FontMetrics fm = g2.getFontMetrics();
                    String init = user.getUsername().substring(0, 1).toUpperCase();
                    g2.drawString(init, (s - fm.stringWidth(init)) / 2, s / 2 + fm.getAscent() / 3);
                }

                g2.setClip(null);
                g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 50));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(0, 0, s - 1, s - 1);
                
                // Hover effect hint
                if (getMousePosition() != null) {
                    g2.setClip(clip);
                    g2.setColor(new Color(0, 0, 0, 150));
                    g2.fillOval(0, 0, s, s);
                    g2.setColor(Color.WHITE);
                    g2.setFont(Constants.FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Upload", (s - fm.stringWidth("Upload")) / 2, s / 2 + fm.getAscent() / 3);
                    g2.setClip(null);
                }
                
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(90, 90));
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { avatar.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { avatar.repaint(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
                if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        java.nio.file.Files.copy(
                            chooser.getSelectedFile().toPath(), 
                            new java.io.File("user_avatar.png").toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                        refresh();
                        mainFrame.reloadAvatar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        profileCard.add(avatar);

        // User info
        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(Constants.FONT_TITLE);
        nameLabel.setForeground(Constants.TEXT_PRIMARY);
        infoBox.add(nameLabel);
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            JLabel emailLabel = new JLabel(user.getEmail());
            emailLabel.setFont(Constants.FONT_BODY);
            emailLabel.setForeground(Constants.TEXT_SECONDARY);
            infoBox.add(emailLabel);
        }
        String joined = user.getCreatedAt() != null
            ? "Joined " + user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            : "Member";
        JLabel joinedLabel = new JLabel(joined);
        joinedLabel.setFont(Constants.FONT_SMALL);
        joinedLabel.setForeground(Constants.TEXT_MUTED);
        infoBox.add(joinedLabel);
        infoBox.add(Box.createVerticalStrut(6));

        // Additional Profile Fields
        String roleStr = user.getTargetRole() != null ? user.getTargetRole() : "Role not set";
        String expStr = user.getExperienceLevel() != null ? user.getExperienceLevel() : "Experience not set";
        String techStr = user.getTechStack() != null ? user.getTechStack() : "Tech stack not set";
        JLabel detailsLabel = new JLabel(roleStr + " | " + expStr + " | " + techStr);
        detailsLabel.setFont(Constants.FONT_SMALL);
        detailsLabel.setForeground(Constants.ACCENT_INFO);
        infoBox.add(detailsLabel);
        infoBox.add(Box.createVerticalStrut(6));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton editBtn = new JButton("Edit Profile");
        editBtn.setFont(Constants.FONT_SMALL);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> showEditProfileDialog());
        buttonRow.add(editBtn);
        
        buttonRow.add(Box.createHorizontalStrut(10));

        JButton syncBtn = new JButton("Cloud Sync");
        syncBtn.setFont(Constants.FONT_SMALL);
        syncBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        syncBtn.addActionListener(e -> simulateCloudSync());
        buttonRow.add(syncBtn);

        buttonRow.add(Box.createHorizontalStrut(10));

        JButton exportBtn = new JButton("Export PDF");
        exportBtn.setFont(Constants.FONT_SMALL);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportPdfReport());
        buttonRow.add(exportBtn);

        infoBox.add(buttonRow);
        infoBox.add(Box.createVerticalStrut(6));

        int badges = achievementService.getEarnedCount(user.getId());
        JLabel badgeLabel = new JLabel("[*] " + badges + " / " + AchievementService.ACHIEVEMENTS.length + " achievements");
        badgeLabel.setFont(Constants.FONT_SMALL);
        badgeLabel.setForeground(Constants.ACCENT_WARNING);
        infoBox.add(badgeLabel);
        profileCard.add(infoBox);

        content.add(profileCard);
        content.add(Box.createVerticalStrut(28));

        // Stats row
        int userId = user.getId();
        PerformanceStats overall = analytics.getOverallStats(userId);
        int totalQuizzes = analytics.getTotalQuizCount(userId);
        int currentStreak = streakService.getCurrentStreak(userId);
        int longestStreak = streakService.getLongestStreak(userId);
        int bookmarks = bookmarkService.getBookmarkCount(userId);

        JPanel statsRow = new JPanel(new GridLayout(1, 5, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsRow.add(createMiniStatCard("Quizzes", String.valueOf(totalQuizzes), Constants.ACCENT_PRIMARY));
        statsRow.add(createMiniStatCard("Accuracy", Constants.formatPercentage(overall.getAccuracy()), Constants.ACCENT_SUCCESS));
        statsRow.add(createMiniStatCard("Current Streak", currentStreak + " days", Constants.ACCENT_ORANGE));
        statsRow.add(createMiniStatCard("Best Streak", longestStreak + " days", Constants.ACCENT_WARNING));
        statsRow.add(createMiniStatCard("Bookmarks", String.valueOf(bookmarks), Constants.ACCENT_INFO));

        content.add(statsRow);
        content.add(Box.createVerticalStrut(28));

        // Bottom row
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Overall accuracy circle
        JPanel accCard = createCardPanel();
        accCard.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 16));
        CircularProgress accCircle = new CircularProgress(140);
        double acc = overall.getAccuracy();
        accCircle.setProgressColor(acc >= 70 ? Constants.ACCENT_SUCCESS : acc >= 40 ? Constants.ACCENT_WARNING : Constants.ACCENT_DANGER);
        accCircle.setLabel("Overall");
        accCircle.setStrokeWidth(12);
        accCircle.setPercentage(acc);
        accCard.add(accCircle);

        JPanel accStats = new JPanel();
        accStats.setOpaque(false);
        accStats.setLayout(new BoxLayout(accStats, BoxLayout.Y_AXIS));
        accStats.add(createStatLine("Correct", String.valueOf(overall.getCorrectCount()), Constants.ACCENT_SUCCESS));
        accStats.add(Box.createVerticalStrut(8));
        accStats.add(createStatLine("Total", String.valueOf(overall.getTotalAttempted()), Constants.TEXT_PRIMARY));
        accStats.add(Box.createVerticalStrut(8));
        accStats.add(createStatLine("Best Topic", analytics.getBestCategory(user.getId()), Constants.ACCENT_WARNING));
        accCard.add(accStats);
        bottomRow.add(accCard);

        // Recent achievements
        JPanel achCard = createCardPanel();
        achCard.setLayout(new BoxLayout(achCard, BoxLayout.Y_AXIS));
        achCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel achTitle = new JLabel("Recent Achievements");
        achTitle.setFont(Constants.FONT_SUBHEADING);
        achTitle.setForeground(Constants.TEXT_PRIMARY);
        achTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        achCard.add(achTitle);
        achCard.add(Box.createVerticalStrut(12));

        java.util.List<String> earned = achievementService.getEarnedAchievements(userId);
        if (earned.isEmpty()) {
            JLabel noAch = new JLabel("Complete quizzes to earn badges!");
            noAch.setFont(Constants.FONT_BODY);
            noAch.setForeground(Constants.TEXT_MUTED);
            noAch.setAlignmentX(Component.LEFT_ALIGNMENT);
            achCard.add(noAch);
        } else {
            int shown = 0;
            for (int i = earned.size() - 1; i >= 0 && shown < 4; i--) {
                String[] def = AchievementService.getDefinition(earned.get(i));
                if (def != null) {
                    JPanel achRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
                    achRow.setOpaque(false);
                    achRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                    achRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                    JLabel achIcon = new JLabel(def[3]);
                    achIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                    achRow.add(achIcon);
                    JLabel achName = new JLabel(def[1]);
                    achName.setFont(Constants.FONT_BODY_BOLD);
                    achName.setForeground(Constants.TEXT_PRIMARY);
                    achRow.add(achName);
                    JLabel achDesc = new JLabel(" — " + def[2]);
                    achDesc.setFont(Constants.FONT_SMALL);
                    achDesc.setForeground(Constants.TEXT_MUTED);
                    achRow.add(achDesc);
                    achCard.add(achRow);
                    shown++;
                }
            }
        }
        achCard.add(Box.createVerticalGlue());
        bottomRow.add(achCard);

        content.add(bottomRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createMiniStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 20));
        valLbl.setForeground(Constants.TEXT_PRIMARY);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(Constants.FONT_SMALL);
        lblLbl.setForeground(Constants.TEXT_MUTED);
        lblLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblLbl);
        return card;
    }

    private JPanel createCardPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
    }

    private JPanel createStatLine(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(200, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(Constants.FONT_SMALL);
        lbl.setForeground(Constants.TEXT_MUTED);
        row.add(lbl, BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(Constants.FONT_BODY_BOLD);
        val.setForeground(valueColor);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private void showEditProfileDialog() {
        User user = mainFrame.getCurrentUser();
        if (user == null) return;

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField roleField = new JTextField(user.getTargetRole() != null ? user.getTargetRole() : "");
        JTextField expField = new JTextField(user.getExperienceLevel() != null ? user.getExperienceLevel() : "");
        JTextField techField = new JTextField(user.getTechStack() != null ? user.getTechStack() : "");

        panel.add(new JLabel("Target Role (e.g. Backend):"));
        panel.add(roleField);
        panel.add(new JLabel("Experience Level (e.g. Mid):"));
        panel.add(expField);
        panel.add(new JLabel("Tech Stack (e.g. Java, Spring):"));
        panel.add(techField);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            user.setTargetRole(roleField.getText().trim());
            user.setExperienceLevel(expField.getText().trim());
            user.setTechStack(techField.getText().trim());
            
            AuthService auth = new AuthService();
            if (auth.updateProfile(user)) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simulateCloudSync() {
        JDialog syncDialog = new JDialog(mainFrame, "Cloud Synchronization", true);
        syncDialog.setSize(300, 150);
        syncDialog.setLocationRelativeTo(mainFrame);
        syncDialog.setLayout(new BorderLayout());
        syncDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("Syncing local database to cloud server...");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Constants.ACCENT_INFO);
        content.add(progressBar);

        syncDialog.add(content, BorderLayout.CENTER);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                CloudSyncService syncService = new CloudSyncService();
                return syncService.syncUserData(mainFrame.getCurrentUser());
            }

            @Override
            protected void done() {
                syncDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame, "Cloud Synchronization Complete!\nYour progress is safely backed up to the Spring Boot REST API.", "Sync Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Cloud Synchronization Failed!\nIs the Spring Boot server running?", "Sync Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, "Cloud Synchronization Error:\n" + e.getMessage(), "Sync Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();

        syncDialog.setVisible(true);
    }

    private void exportPdfReport() {
        User user = mainFrame.getCurrentUser();
        if (user == null) return;
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File("InterviewMentor_Report_" + user.getUsername() + ".pdf"));
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            boolean success = pdfExportService.exportReport(user, fileToSave.getAbsolutePath());
            if (success) {
                com.interviewmentor.view.components.ToastNotification.success("PDF exported successfully to " + fileToSave.getName());
            } else {
                com.interviewmentor.view.components.ToastNotification.error("Failed to export PDF.");
            }
        }
    }
}
