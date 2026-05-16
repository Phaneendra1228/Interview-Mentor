package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Radar/Spider chart component for visualizing multi-dimensional skill profiles.
 * Renders category axes, grid rings, and a filled data polygon with animation.
 */
public class RadarChart extends JPanel {

    private final List<String> labels = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();   // 0-100
    private final List<Color> colors = new ArrayList<>();
    private float animProgress = 0f;
    private Timer animTimer;

    public RadarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 300));
    }

    public void addAxis(String label, double value, Color color) {
        labels.add(label);
        values.add(Math.max(0, Math.min(100, value)));
        colors.add(color);
    }

    public void clearData() {
        labels.clear(); values.clear(); colors.clear();
    }

    public void animate() {
        animProgress = 0f;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(16, e -> {
            animProgress += 0.04f;
            if (animProgress >= 1f) { animProgress = 1f; animTimer.stop(); }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int n = labels.size();
        if (n < 3) { g2.dispose(); return; }

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2;
        int radius = Math.min(cx, cy) - 40;

        // Draw grid rings
        g2.setStroke(new BasicStroke(1f));
        for (int ring = 1; ring <= 5; ring++) {
            int r = radius * ring / 5;
            g2.setColor(Constants.withAlpha(Constants.BORDER_COLOR, 40 + ring * 15));
            drawPolygon(g2, cx, cy, r, n, false);
        }

        // Draw axes
        g2.setColor(Constants.withAlpha(Constants.BORDER_COLOR, 80));
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            int ax = cx + (int)(radius * Math.cos(angle));
            int ay = cy + (int)(radius * Math.sin(angle));
            g2.drawLine(cx, cy, ax, ay);
        }

        // Draw data polygon
        float ap = animProgress;
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            double val = values.get(i) / 100.0 * ap;
            int px = cx + (int)(radius * val * Math.cos(angle));
            int py = cy + (int)(radius * val * Math.sin(angle));
            if (i == 0) path.moveTo(px, py);
            else path.lineTo(px, py);
        }
        path.closePath();

        // Fill with gradient
        g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 30));
        g2.fill(path);
        g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY_LIGHT, 180));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(path);

        // Draw data points
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            double val = values.get(i) / 100.0 * ap;
            int px = cx + (int)(radius * val * Math.cos(angle));
            int py = cy + (int)(radius * val * Math.sin(angle));
            g2.setColor(colors.get(i));
            g2.fillOval(px - 5, py - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 2, py - 2, 4, 4);
        }

        // Draw labels
        g2.setFont(Constants.FONT_SMALL);
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            int lx = cx + (int)((radius + 24) * Math.cos(angle));
            int ly = cy + (int)((radius + 24) * Math.sin(angle));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(labels.get(i));

            g2.setColor(colors.get(i));
            g2.drawString(labels.get(i), lx - tw / 2, ly + fm.getAscent() / 3);

            // Value percentage below label
            String valStr = String.format("%.0f%%", values.get(i) * ap);
            g2.setColor(Constants.TEXT_MUTED);
            g2.setFont(Constants.FONT_TINY);
            int vw = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, lx - vw / 2, ly + fm.getAscent() / 3 + 14);
            g2.setFont(Constants.FONT_SMALL);
        }

        g2.dispose();
    }

    private void drawPolygon(Graphics2D g2, int cx, int cy, int r, int n, boolean fill) {
        int[] xp = new int[n], yp = new int[n];
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
            xp[i] = cx + (int)(r * Math.cos(angle));
            yp[i] = cy + (int)(r * Math.sin(angle));
        }
        if (fill) g2.fillPolygon(xp, yp, n);
        else g2.drawPolygon(xp, yp, n);
    }
}
