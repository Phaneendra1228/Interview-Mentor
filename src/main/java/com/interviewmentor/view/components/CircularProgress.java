package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Circular progress indicator that renders a percentage as an arc.
 */
public class CircularProgress extends JPanel {

    private double percentage = 0;
    private Color progressColor = Constants.ACCENT_PRIMARY;
    private Color trackColor = Constants.BORDER_COLOR;
    private String label = "";
    private int strokeWidth = 10;
    private boolean showPercentText = true;
    private double animatedPercentage = 0;
    private Timer animTimer;

    public CircularProgress(int size) {
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void setPercentage(double percentage) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        animateToTarget();
    }

    private void animateToTarget() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(16, e -> {
            double diff = percentage - animatedPercentage;
            if (Math.abs(diff) < 0.5) {
                animatedPercentage = percentage;
                ((Timer) e.getSource()).stop();
            } else {
                animatedPercentage += diff * 0.08;
            }
            repaint();
        });
        animTimer.start();
    }

    public void setProgressColor(Color color) {
        this.progressColor = color;
        repaint();
    }

    public void setLabel(String label) {
        this.label = label;
        repaint();
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
    }

    public void setShowPercentText(boolean show) {
        this.showPercentText = show;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int padding = strokeWidth + 4;
        int x = (getWidth() - size) / 2 + padding;
        int y = (getHeight() - size) / 2 + padding;
        int arcSize = size - 2 * padding;

        // Track (background circle)
        g2.setColor(trackColor);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(x, y, arcSize, arcSize, 0, 360);

        // Progress arc
        if (animatedPercentage > 0) {
            g2.setColor(progressColor);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int angle = (int) (animatedPercentage * 360 / 100);
            g2.drawArc(x, y, arcSize, arcSize, 90, -angle);
        }

        // Center text
        if (showPercentText) {
            String text = String.format("%.0f%%", animatedPercentage);
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, size / 5));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = getHeight() / 2 + fm.getAscent() / 3;
            g2.drawString(text, textX, textY);
        }

        // Label below percentage
        if (label != null && !label.isEmpty()) {
            g2.setColor(Constants.TEXT_SECONDARY);
            g2.setFont(Constants.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int labelX = (getWidth() - fm.stringWidth(label)) / 2;
            int labelY = getHeight() / 2 + size / 6;
            g2.drawString(label, labelX, labelY);
        }

        g2.dispose();
    }
}
