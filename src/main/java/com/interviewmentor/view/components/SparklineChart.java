package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * A mini inline sparkline chart component for showing trend data.
 * Supports smooth animated line drawing with area fill gradient.
 */
public class SparklineChart extends JPanel {

    private final List<Double> data = new ArrayList<>();
    private Color lineColor = Constants.ACCENT_PRIMARY;
    private Color fillColor = Constants.withAlpha(Constants.ACCENT_PRIMARY, 30);
    private float animProgress = 0f;
    private Timer animTimer;
    private int strokeWidth = 2;

    public SparklineChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(120, 40));
    }

    public SparklineChart(int width, int height) {
        setOpaque(false);
        setPreferredSize(new Dimension(width, height));
    }

    public void setData(List<Double> values) {
        data.clear();
        data.addAll(values);
        animate();
    }

    public void setLineColor(Color color) {
        this.lineColor = color;
        this.fillColor = Constants.withAlpha(color, 30);
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
    }

    public void animate() {
        animProgress = 0f;
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(16, e -> {
            animProgress += 0.05f;
            if (animProgress >= 1f) {
                animProgress = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 4;
        int drawW = w - 2 * padding;
        int drawH = h - 2 * padding;

        // Find min/max
        double min = data.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = data.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        if (max == min) { max = min + 1; }
        double range = max - min;

        // Calculate points
        int pointCount = data.size();
        int visiblePoints = (int) Math.ceil(pointCount * animProgress);
        if (visiblePoints < 2) visiblePoints = Math.min(2, pointCount);

        float[] xPoints = new float[visiblePoints];
        float[] yPoints = new float[visiblePoints];

        for (int i = 0; i < visiblePoints; i++) {
            xPoints[i] = padding + (float) i / (pointCount - 1) * drawW;
            double normalized = (data.get(i) - min) / range;
            yPoints[i] = padding + drawH - (float)(normalized * drawH);
        }

        // Draw area fill
        GeneralPath area = new GeneralPath();
        area.moveTo(xPoints[0], h - padding);
        for (int i = 0; i < visiblePoints; i++) {
            if (i == 0) {
                area.lineTo(xPoints[i], yPoints[i]);
            } else {
                // Smooth curve using Catmull-Rom to Bezier
                float cx = (xPoints[i - 1] + xPoints[i]) / 2;
                area.quadTo(cx, yPoints[i - 1], xPoints[i], yPoints[i]);
            }
        }
        area.lineTo(xPoints[visiblePoints - 1], h - padding);
        area.closePath();

        GradientPaint areaGrad = new GradientPaint(0, 0, fillColor, 0, h, new Color(0, 0, 0, 0));
        g2.setPaint(areaGrad);
        g2.fill(area);

        // Draw line
        GeneralPath line = new GeneralPath();
        for (int i = 0; i < visiblePoints; i++) {
            if (i == 0) {
                line.moveTo(xPoints[i], yPoints[i]);
            } else {
                float cx = (xPoints[i - 1] + xPoints[i]) / 2;
                line.quadTo(cx, yPoints[i - 1], xPoints[i], yPoints[i]);
            }
        }
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(line);

        // Draw end dot
        if (visiblePoints > 0) {
            int lastIdx = visiblePoints - 1;
            g2.setColor(lineColor);
            g2.fillOval((int) xPoints[lastIdx] - 3, (int) yPoints[lastIdx] - 3, 6, 6);
            g2.setColor(Constants.withAlpha(lineColor, 60));
            g2.fillOval((int) xPoints[lastIdx] - 6, (int) yPoints[lastIdx] - 6, 12, 12);
        }

        g2.dispose();
    }
}
