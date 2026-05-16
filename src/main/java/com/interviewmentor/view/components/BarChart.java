package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A horizontal bar chart component for displaying category-wise performance.
 */
public class BarChart extends JPanel {

    private final List<BarData> bars = new ArrayList<>();
    private String title = "";
    private int barHeight = 28;
    private int barGap = 14;
    private double maxValue = 100;
    private float animProgress = 0f;
    private Timer animTimer;

    public BarChart() {
        setOpaque(false);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void addBar(String label, double value, Color color) {
        bars.add(new BarData(label, value, color));
        recalcSize();
    }

    public void clearBars() {
        bars.clear();
        animProgress = 0f;
        recalcSize();
    }

    public void animateBars() {
        animProgress = 0f;
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(16, e -> {
            animProgress += 0.04f;
            if (animProgress >= 1f) {
                animProgress = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animTimer.start();
    }

    private void recalcSize() {
        int titleH = title.isEmpty() ? 0 : 40;
        int totalH = titleH + bars.size() * (barHeight + barGap) + 20;
        setPreferredSize(new Dimension(400, Math.max(totalH, 100)));
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int yOffset = 0;

        // Title
        if (!title.isEmpty()) {
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(Constants.FONT_SUBHEADING);
            g2.drawString(title, 0, 20);
            yOffset = 40;
        }

        int labelWidth = 140;
        int barAreaWidth = w - labelWidth - 70;

        for (int i = 0; i < bars.size(); i++) {
            BarData bar = bars.get(i);
            int barY = yOffset + i * (barHeight + barGap);

            // Label
            g2.setColor(Constants.TEXT_SECONDARY);
            g2.setFont(Constants.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(bar.label, 0, barY + barHeight / 2 + fm.getAscent() / 3);

            // Track
            g2.setColor(Constants.BG_CARD);
            RoundRectangle2D track = new RoundRectangle2D.Float(
                labelWidth, barY, barAreaWidth, barHeight, 8, 8
            );
            g2.fill(track);

            // Bar fill (animated)
            double ratio = (bar.value / maxValue) * animProgress;
            int fillWidth = (int)(barAreaWidth * ratio);
            if (fillWidth > 0) {
                GradientPaint gp = new GradientPaint(
                    labelWidth, barY, bar.color,
                    labelWidth + fillWidth, barY, bar.color.brighter()
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(
                    labelWidth, barY, fillWidth, barHeight, 8, 8
                ));
            }

            // Value text
            String valText = String.format("%.0f%%", bar.value);
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(Constants.FONT_SMALL);
            fm = g2.getFontMetrics();
            g2.drawString(valText, labelWidth + barAreaWidth + 10,
                barY + barHeight / 2 + fm.getAscent() / 3);
        }

        g2.dispose();
    }

    private static class BarData {
        String label;
        double value;
        Color color;

        BarData(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
}
