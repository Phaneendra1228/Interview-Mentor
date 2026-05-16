package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Animated slide-in toast notifications that appear at the top-right of the window.
 * Supports success, info, warning, and error types with auto-dismiss.
 */
public class ToastNotification {

    public enum Type { SUCCESS, INFO, WARNING, ERROR }

    private static final Queue<ToastPanel> activeToasts = new LinkedList<>();
    private static JLayeredPane layeredPane;

    /** Initialize the toast system with the frame's layered pane */
    public static void init(JFrame frame) {
        layeredPane = frame.getLayeredPane();
    }

    /** Show a toast notification */
    public static void show(String message, Type type) {
        if (layeredPane == null) return;

        SwingUtilities.invokeLater(() -> {
            ToastPanel toast = new ToastPanel(message, type);
            int toastWidth = 360;
            int toastHeight = 56;
            int margin = 20;
            int yOffset = margin + activeToasts.size() * (toastHeight + 8);

            toast.setBounds(
                layeredPane.getWidth() - toastWidth - margin,
                yOffset,
                toastWidth,
                toastHeight
            );

            activeToasts.add(toast);
            layeredPane.add(toast, JLayeredPane.POPUP_LAYER);
            layeredPane.revalidate();
            layeredPane.repaint();

            // Slide-in animation
            toast.animateIn();

            // Auto-dismiss after duration
            Timer dismissTimer = new Timer(Constants.TOAST_DURATION, e -> {
                toast.animateOut(() -> {
                    layeredPane.remove(toast);
                    activeToasts.remove(toast);
                    repositionToasts();
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            });
            dismissTimer.setRepeats(false);
            dismissTimer.start();
        });
    }

    /** Convenience methods */
    public static void success(String msg) { show(msg, Type.SUCCESS); }
    public static void info(String msg)    { show(msg, Type.INFO); }
    public static void warning(String msg) { show(msg, Type.WARNING); }
    public static void error(String msg)   { show(msg, Type.ERROR); }

    private static void repositionToasts() {
        int margin = 20;
        int toastHeight = 56;
        int index = 0;
        for (ToastPanel toast : activeToasts) {
            int targetY = margin + index * (toastHeight + 8);
            toast.setLocation(toast.getX(), targetY);
            index++;
        }
    }

    // ====== Inner Toast Panel ======
    private static class ToastPanel extends JPanel {
        private float opacity = 0f;
        private float slideOffset = 40f;
        private final String message;
        private final Type type;

        ToastPanel(String message, Type type) {
            this.message = message;
            this.type = type;
            setOpaque(false);
        }

        void animateIn() {
            Timer timer = new Timer(16, null);
            timer.addActionListener(e -> {
                opacity = Math.min(1f, opacity + 0.1f);
                slideOffset = Math.max(0, slideOffset - 5f);
                if (opacity >= 1f && slideOffset <= 0) {
                    timer.stop();
                }
                repaint();
            });
            timer.start();
        }

        void animateOut(Runnable onComplete) {
            Timer timer = new Timer(16, null);
            timer.addActionListener(e -> {
                opacity = Math.max(0f, opacity - 0.08f);
                slideOffset += 3f;
                if (opacity <= 0) {
                    timer.stop();
                    onComplete.run();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            int w = getWidth();
            int h = getHeight();

            // Translate for slide effect
            g2.translate(slideOffset, 0);

            // Background with glassmorphism
            g2.setColor(new Color(15, 23, 42, 230));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

            // Border
            g2.setColor(new Color(51, 65, 85, 100));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));

            // Left accent bar
            Color accentColor = switch (type) {
                case SUCCESS -> Constants.ACCENT_SUCCESS;
                case INFO    -> Constants.ACCENT_INFO;
                case WARNING -> Constants.ACCENT_WARNING;
                case ERROR   -> Constants.ACCENT_DANGER;
            };
            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, 4, h, 4, 4));

            // Draw icon shape (painted, not font-dependent)
            g2.setColor(accentColor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = 24, cy = h / 2;
            switch (type) {
                case SUCCESS -> {
                    // Checkmark
                    g2.drawLine(cx - 6, cy, cx - 2, cy + 5);
                    g2.drawLine(cx - 2, cy + 5, cx + 7, cy - 5);
                }
                case INFO -> {
                    // Info circle with "i"
                    g2.drawOval(cx - 8, cy - 8, 16, 16);
                    g2.fillOval(cx - 2, cy - 6, 4, 4);
                    g2.drawLine(cx, cy - 1, cx, cy + 6);
                }
                case WARNING -> {
                    // Warning triangle with "!"
                    int[] xp = {cx, cx - 9, cx + 9};
                    int[] yp = {cy - 8, cy + 7, cy + 7};
                    g2.drawPolygon(xp, yp, 3);
                    g2.drawLine(cx, cy - 3, cx, cy + 2);
                    g2.fillOval(cx - 1, cy + 4, 3, 3);
                }
                case ERROR -> {
                    // X mark
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                }
            }

            // Message text
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(Constants.FONT_BODY);
            FontMetrics fm = g2.getFontMetrics();
            String displayMsg = message;
            int maxTextWidth = w - 60;
            if (fm.stringWidth(displayMsg) > maxTextWidth) {
                while (fm.stringWidth(displayMsg + "...") > maxTextWidth && displayMsg.length() > 0) {
                    displayMsg = displayMsg.substring(0, displayMsg.length() - 1);
                }
                displayMsg += "...";
            }
            g2.drawString(displayMsg, 42, h / 2 + fm.getAscent() / 3);

            g2.dispose();
        }
    }
}
