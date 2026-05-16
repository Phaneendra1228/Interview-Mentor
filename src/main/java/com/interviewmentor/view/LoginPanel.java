package com.interviewmentor.view;

import com.interviewmentor.model.User;
import com.interviewmentor.service.AuthService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.GradientPanel;
import com.interviewmentor.view.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login and registration screen with animated gradient background and Enter key support.
 */
public class LoginPanel extends GradientPanel {

    private final MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JLabel statusLabel;
    private boolean registerMode = false;
    private JPanel formCard;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel toggleLabel;
    private JLabel forgotPasswordLabel;
    private RoundedButton actionButton;
    private JPanel usernameRow;

    private final AuthService authService = new AuthService();

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.GLASS_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(Constants.GLASS_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        formCard.setPreferredSize(new Dimension(440, 520));

        // Brand circle
        JPanel brandCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Constants.ACCENT_PRIMARY, getWidth(), getHeight(), Constants.GRADIENT_END));
                g2.fillOval(0, 0, 64, 64);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brandCircle.setOpaque(false);
        brandCircle.setPreferredSize(new Dimension(64, 64));
        brandCircle.setMaximumSize(new Dimension(64, 64));
        brandCircle.setLayout(new GridBagLayout());
        JLabel iconLetter = new JLabel("IM");
        iconLetter.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 22));
        iconLetter.setForeground(Color.WHITE);
        brandCircle.add(iconLetter);
        brandCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(brandCircle);
        formCard.add(Box.createVerticalStrut(16));

        titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(Constants.FONT_HEADING);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Technical Interview Preparation Trainer");
        subtitleLabel.setFont(Constants.FONT_SMALL);
        subtitleLabel.setForeground(Constants.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(subtitleLabel);
        formCard.add(Box.createVerticalStrut(32));

        emailLabel = createLabel("Email / Username");
        formCard.add(emailLabel);
        formCard.add(Box.createVerticalStrut(6));
        emailField = createStyledTextField("Enter your email or username");
        formCard.add(emailField);
        formCard.add(Box.createVerticalStrut(16));

        usernameRow = new JPanel();
        usernameRow.setOpaque(false);
        usernameRow.setLayout(new BoxLayout(usernameRow, BoxLayout.Y_AXIS));
        usernameRow.add(createLabel("Username"));
        usernameRow.add(Box.createVerticalStrut(6));
        usernameField = createStyledTextField("Enter your username");
        usernameRow.add(usernameField);
        usernameRow.add(Box.createVerticalStrut(16));
        usernameRow.setVisible(false);
        usernameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        usernameRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(usernameRow);

        formCard.add(createLabel("Password"));
        formCard.add(Box.createVerticalStrut(6));
        
        JPanel passWrap = new JPanel(new BorderLayout(5, 0));
        passWrap.setOpaque(false);
        passWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = new JPasswordField();
        styleField(passwordField);
        
        JButton showPassBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? Constants.TEXT_PRIMARY : Constants.TEXT_MUTED);
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                java.awt.geom.Path2D.Float eye = new java.awt.geom.Path2D.Float();
                eye.moveTo(cx - 8, cy);
                eye.quadTo(cx, cy - 6, cx + 8, cy);
                eye.quadTo(cx, cy + 6, cx - 8, cy);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(eye);
                g2.drawOval(cx - 2, cy - 2, 4, 4);
                
                if ("Hide".equals(getName())) {
                    g2.drawLine(cx - 7, cy - 5, cx + 7, cy + 5);
                }
                g2.dispose();
            }
        };
        showPassBtn.setName("Show");
        showPassBtn.setToolTipText("Show Password");
        showPassBtn.setPreferredSize(new Dimension(40, 20));
        showPassBtn.setOpaque(false);
        showPassBtn.setContentAreaFilled(false);
        showPassBtn.setBorderPainted(false);
        showPassBtn.setFocusPainted(false);
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String passPlaceholder = "Enter your password";
        passwordField.setToolTipText(passPlaceholder);
        passwordField.setForeground(Constants.TEXT_MUTED);
        passwordField.setText(passPlaceholder);
        passwordField.setEchoChar((char) 0);
        
        passwordField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals(passPlaceholder)) {
                    passwordField.setText("");
                    passwordField.setForeground(Constants.TEXT_PRIMARY);
                    if ("Show".equals(showPassBtn.getName())) {
                        passwordField.setEchoChar('\u2022');
                    }
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setForeground(Constants.TEXT_MUTED);
                    passwordField.setText(passPlaceholder);
                    passwordField.setEchoChar((char) 0);
                }
            }
        });

        showPassBtn.addActionListener(e -> {
            if (String.valueOf(passwordField.getPassword()).equals(passPlaceholder)) return;
            
            if (passwordField.getEchoChar() != (char) 0) {
                passwordField.setEchoChar((char) 0);
                showPassBtn.setName("Hide");
                showPassBtn.setToolTipText("Hide Password");
            } else {
                passwordField.setEchoChar('\u2022');
                showPassBtn.setName("Show");
                showPassBtn.setToolTipText("Show Password");
            }
            showPassBtn.repaint();
        });
        
        passWrap.add(passwordField, BorderLayout.CENTER);
        passWrap.add(showPassBtn, BorderLayout.EAST);
        formCard.add(passWrap);
        formCard.add(Box.createVerticalStrut(24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Constants.FONT_SMALL);
        statusLabel.setForeground(Constants.ACCENT_DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(8));

        actionButton = new RoundedButton("Sign In");
        actionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.addActionListener(e -> handleAction());
        formCard.add(actionButton);
        formCard.add(Box.createVerticalStrut(12));

        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(Constants.FONT_SMALL);
        forgotPasswordLabel.setForeground(Constants.ACCENT_PRIMARY_LIGHT);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleForgotPassword(); }
            @Override public void mouseEntered(MouseEvent e) { forgotPasswordLabel.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { forgotPasswordLabel.setForeground(Constants.ACCENT_PRIMARY_LIGHT); }
        });
        formCard.add(forgotPasswordLabel);
        formCard.add(Box.createVerticalStrut(12));

        toggleLabel = new JLabel("Don't have an account? Sign Up");
        toggleLabel.setFont(Constants.FONT_SMALL);
        toggleLabel.setForeground(Constants.ACCENT_PRIMARY_LIGHT);
        toggleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggleLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { toggleMode(); }
            @Override public void mouseEntered(MouseEvent e) { toggleLabel.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { toggleLabel.setForeground(Constants.ACCENT_PRIMARY_LIGHT); }
        });
        formCard.add(toggleLabel);
        formCard.add(Box.createVerticalStrut(20));

        // Social Login Section
        JPanel dividerPanel = new JPanel(new BorderLayout());
        dividerPanel.setOpaque(false);
        dividerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        dividerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSeparator sepLeft = new JSeparator();
        JSeparator sepRight = new JSeparator();
        sepLeft.setForeground(Constants.BORDER_COLOR);
        sepRight.setForeground(Constants.BORDER_COLOR);
        JLabel orLabel = new JLabel(" or continue with ");
        orLabel.setFont(Constants.FONT_TINY);
        orLabel.setForeground(Constants.TEXT_MUTED);
        dividerPanel.add(sepLeft, BorderLayout.WEST);
        dividerPanel.add(orLabel, BorderLayout.CENTER);
        dividerPanel.add(sepRight, BorderLayout.EAST);
        formCard.add(dividerPanel);
        formCard.add(Box.createVerticalStrut(12));

        JPanel socialPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        socialPanel.setOpaque(false);
        socialPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        socialPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton googleBtn = new JButton("Google");
        googleBtn.setFont(Constants.FONT_BODY_BOLD);
        googleBtn.setBackground(Constants.BG_CARD);
        googleBtn.setForeground(Constants.TEXT_PRIMARY);
        googleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleBtn.setFocusPainted(false);
        googleBtn.addActionListener(e -> startRealOAuth("Google"));
        socialPanel.add(googleBtn);

        JButton githubBtn = new JButton("GitHub");
        githubBtn.setFont(Constants.FONT_BODY_BOLD);
        githubBtn.setBackground(Constants.BG_CARD);
        githubBtn.setForeground(Constants.TEXT_PRIMARY);
        githubBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        githubBtn.setFocusPainted(false);
        githubBtn.addActionListener(e -> startRealOAuth("GitHub"));
        socialPanel.add(githubBtn);

        formCard.add(socialPanel);

        // Enter key to submit
        KeyListener enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleAction();
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);
        emailField.addKeyListener(enterKey);

        add(formCard);
    }

    private void toggleMode() {
        registerMode = !registerMode;
        usernameRow.setVisible(registerMode);
        forgotPasswordLabel.setVisible(!registerMode);
        titleLabel.setText(registerMode ? "Create Account" : "Welcome Back");
        emailLabel.setText(registerMode ? "Email" : "Email / Username");
        actionButton.setText(registerMode ? "Sign Up" : "Sign In");
        toggleLabel.setText(registerMode
            ? "Already have an account? Sign In"
            : "Don't have an account? Sign Up");
        statusLabel.setText(" ");
        formCard.revalidate();
        formCard.repaint();
    }

    private void handleAction() {
        String identifier = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (identifier.isEmpty() || password.isEmpty() ||
            identifier.equals("Enter your email or username") ||
            identifier.equals("Enter your email") ||
            password.equals("Enter your password")) {
            showError("Please fill in all fields.");
            return;
        }

        if (registerMode) {
            String username = usernameField.getText().trim();
            if (username.isEmpty() || username.equals("Enter your username")) {
                showError("Please enter a username.");
                return;
            }
            if (password.length() < 4) {
                showError("Password must be at least 4 characters.");
                return;
            }
            if (authService.usernameExists(username)) {
                showError("Username already taken.");
                return;
            }
            User user = authService.register(username, identifier, password);
            if (user != null) {
                mainFrame.onLoginSuccess(user);
            } else {
                showError("Registration failed. Try again.");
            }
        } else {
            User user = authService.login(identifier, password);
            if (user != null) {
                mainFrame.onLoginSuccess(user);
            } else {
                showError("Invalid email/username or password.");
            }
        }
    }

    private void handleForgotPassword() {
        if (registerMode) toggleMode(); // Switch to login mode
        String email = emailField.getText().trim();
        if (email.isEmpty() || email.equals("Enter your email")) {
            showError("Please enter your email above to reset password.");
            return;
        }
        if (!authService.emailExists(email)) {
            showError("No account found with this email.");
            return;
        }
        String newPass = JOptionPane.showInputDialog(this, "Enter a new password for " + email + ":", "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPass != null && newPass.trim().length() >= 4) {
            if (authService.updatePassword(email, newPass.trim())) {
                statusLabel.setForeground(Constants.ACCENT_SUCCESS);
                statusLabel.setText("Password updated successfully! You can now sign in.");
            } else {
                showError("Failed to update password.");
            }
        } else if (newPass != null) {
            showError("Password must be at least 4 characters.");
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(Constants.ACCENT_DANGER);
        statusLabel.setText(msg);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Constants.FONT_BODY_BOLD);
        label.setForeground(Constants.TEXT_SECONDARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        styleField(field);
        field.setToolTipText(placeholder);
        field.setForeground(Constants.TEXT_MUTED);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Constants.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Constants.TEXT_MUTED);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private void styleField(JComponent field) {
        field.setFont(Constants.FONT_BODY);
        field.setBackground(Constants.BG_INPUT);
        field.setForeground(Constants.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (field instanceof JTextField) {
            ((JTextField) field).setCaretColor(Constants.TEXT_PRIMARY);
        }
    }

    private void startRealOAuth(String provider) {
        // Show indeterminate progress dialogue
        JDialog oauthDialog = new JDialog(mainFrame, provider + " Authorization", true);
        oauthDialog.setSize(350, 150);
        oauthDialog.setLocationRelativeTo(mainFrame);
        oauthDialog.setLayout(new BorderLayout());
        oauthDialog.getContentPane().setBackground(Constants.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("Waiting for browser authorization...");
        statusLabel.setFont(Constants.FONT_BODY);
        statusLabel.setForeground(Constants.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Constants.ACCENT_INFO);
        content.add(progressBar);

        oauthDialog.add(content, BorderLayout.CENTER);

        // Run the server and browser launch in a background thread so we don't block the EDT
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.interviewmentor.service.OAuthService oauthService = new com.interviewmentor.service.OAuthService();
                oauthService.startOAuthFlow(provider, new com.interviewmentor.service.OAuthService.OAuthCallback() {
                    @Override
                    public void onSuccess(String email, String name) {
                        SwingUtilities.invokeLater(() -> {
                            oauthDialog.dispose();
                            User user = authService.login(email, "oauth_secure_pass");
                            if (user == null) {
                                user = authService.register(name, email, "oauth_secure_pass");
                            }
                            if (user != null) {
                                mainFrame.onLoginSuccess(user);
                            } else {
                                showError(provider + " login processing failed.");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        SwingUtilities.invokeLater(() -> {
                            oauthDialog.dispose();
                            JOptionPane.showMessageDialog(mainFrame, 
                                "OAuth Error: " + error + "\n\nNote: You must replace the placeholders in OAuthService.java with real Client IDs from Google/GitHub developer consoles.", 
                                "Authorization Failed", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                });
                return null;
            }
        }.execute();

        // This will block until oauthDialog.dispose() is called from the callback
        oauthDialog.setVisible(true);
    }
}
