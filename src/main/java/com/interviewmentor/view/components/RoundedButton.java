package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A modern button with gradient fill, rounded corners, hover animation,
 * and Material Design-inspired ripple click effect.
 */
public class RoundedButton extends JButton {

    private Color bgColor;
    private Color bgHoverColor;
    private Color fgColor;
    private boolean hovered = false;
    private boolean useGradient;
    private int radius;
    private float scale = 1.0f;

    // Ripple effect
    private final List<Ripple> ripples = new ArrayList<>();
    private Timer rippleTimer;

    public RoundedButton(String text) {
        this(text, Constants.ACCENT_PRIMARY, Constants.ACCENT_PRIMARY_LIGHT, Color.WHITE, true);
    }

    public RoundedButton(String text, Color bgColor) {
        this(text, bgColor, bgColor.brighter(), Color.WHITE, false);
    }

    public RoundedButton(String text, Color bgColor, Color bgHoverColor, Color fgColor, boolean useGradient) {
        super(text);
        this.bgColor = bgColor;
        this.bgHoverColor = bgHoverColor;
        this.fgColor = fgColor;
        this.useGradient = useGradient;
        this.radius = Constants.BUTTON_RADIUS;

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(fgColor);
        setFont(Constants.FONT_BUTTON);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(160, 44));

        // Ripple animation timer
        rippleTimer = new Timer(16, e -> {
            boolean needsRepaint = false;
            Iterator<Ripple> it = ripples.iterator();
            while (it.hasNext()) {
                Ripple r = it.next();
                r.progress += 0.06f;
                if (r.progress >= 1.5f) {
                    it.remove();
                }
                needsRepaint = true;
            }
            if (needsRepaint) repaint();
            if (ripples.isEmpty()) rippleTimer.stop();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                animateScale(1.03f);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                animateScale(1.0f);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Add ripple at click point
                ripples.add(new Ripple(e.getX(), e.getY()));
                if (!rippleTimer.isRunning()) rippleTimer.start();
            }
        });
    }

    private void animateScale(float target) {
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            float diff = target - scale;
            if (Math.abs(diff) < 0.005f) {
                scale = target;
                timer.stop();
            } else {
                scale += diff * 0.3f;
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Apply scale transform from center
        if (scale != 1.0f) {
            float tx = w * (1 - scale) / 2;
            float ty = h * (1 - scale) / 2;
            g2.translate(tx, ty);
            g2.scale(scale, scale);
        }

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius);

        // Fill
        if (useGradient) {
            Color start = hovered ? bgHoverColor : bgColor;
            Color end = hovered ? bgColor : Constants.GRADIENT_END;
            GradientPaint gp = new GradientPaint(0, 0, start, w, h, end);
            g2.setPaint(gp);
        } else {
            g2.setColor(hovered ? bgHoverColor : bgColor);
        }
        g2.fill(shape);

        // Ripple effects
        if (!ripples.isEmpty()) {
            Shape oldClip = g2.getClip();
            g2.setClip(shape);
            for (Ripple r : ripples) {
                float maxRadius = (float) Math.sqrt(w * w + h * h);
                float rippleRadius = maxRadius * r.progress;
                float alpha = Math.max(0, 0.3f - r.progress * 0.2f);
                g2.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
                g2.fill(new Ellipse2D.Float(
                    r.x - rippleRadius, r.y - rippleRadius,
                    rippleRadius * 2, rippleRadius * 2
                ));
            }
            g2.setClip(oldClip);
        }

        // Subtle border glow on hover
        if (hovered) {
            g2.setColor(Constants.withAlpha(bgColor, 100));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(shape);
        }

        // Text
        g2.setColor(fgColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    public void setColors(Color bg, Color hover) {
        this.bgColor = bg;
        this.bgHoverColor = hover;
        repaint();
    }

    // ====== Ripple data ======
    private static class Ripple {
        float x, y;
        float progress = 0f;
        Ripple(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
