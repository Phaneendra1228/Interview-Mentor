package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * An animated gradient background panel with floating glowing orbs and sparkle particles.
 */
public class GradientPanel extends JPanel {

    private float animationPhase = 0f;
    private Timer animTimer;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rand = new Random();

    public GradientPanel() {
        setOpaque(true);
        // Generate initial particles
        for (int i = 0; i < 30; i++) {
            particles.add(new Particle(rand));
        }
        animTimer = new Timer(40, e -> {
            animationPhase += 0.015f;
            if (animationPhase > (float)(2 * Math.PI)) animationPhase = 0;
            updateParticles();
            repaint();
        });
        animTimer.start();
    }

    private void updateParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.y -= p.speed;
            p.x += Math.sin(p.phase) * 0.3f;
            p.phase += 0.02f;
            p.life -= 0.008f;
            if (p.life <= 0) {
                it.remove();
            }
        }
        // Spawn new particles
        if (particles.size() < 30 && rand.nextFloat() < 0.3f) {
            particles.add(new Particle(rand));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Base gradient
        GradientPaint base = new GradientPaint(
            0, 0, Constants.BG_DARK,
            w, h, new Color(0x0D0D2B)
        );
        g2.setPaint(base);
        g2.fillRect(0, 0, w, h);

        // Floating orbs
        drawOrb(g2, w * 0.15f, h * 0.25f, 220, Constants.ACCENT_PRIMARY, 0.07f, 1.0f);
        drawOrb(g2, w * 0.75f, h * 0.6f, 180, Constants.ACCENT_SECONDARY, 0.06f, 1.3f);
        drawOrb(g2, w * 0.5f, h * 0.85f, 200, Constants.ACCENT_INFO, 0.05f, 0.8f);
        drawOrb(g2, w * 0.85f, h * 0.15f, 140, new Color(0xA855F7), 0.04f, 1.6f);

        // Sparkle particles
        for (Particle p : particles) {
            float px = p.x / 100f * w;
            float py = p.y / 100f * h;
            float alpha = p.life * p.brightness;
            if (alpha > 0 && px >= 0 && px <= w && py >= 0 && py <= h) {
                int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
                g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), a));
                float size = p.size * (0.5f + p.life * 0.5f);
                g2.fill(new Ellipse2D.Float(px - size / 2, py - size / 2, size, size));

                // Cross sparkle effect for brighter particles
                if (p.brightness > 0.6f) {
                    g2.setStroke(new BasicStroke(0.5f));
                    int ca = a / 3;
                    g2.setColor(new Color(255, 255, 255, ca));
                    g2.drawLine((int)(px - size), (int)py, (int)(px + size), (int)py);
                    g2.drawLine((int)px, (int)(py - size), (int)px, (int)(py + size));
                }
            }
        }

        // Subtle grid lines
        g2.setColor(new Color(255, 255, 255, 6));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < w; x += 60) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 60) {
            g2.drawLine(0, y, w, y);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    private void drawOrb(Graphics2D g2, float cx, float cy, float radius,
                          Color color, float alpha, float speedMult) {
        float phase = animationPhase * speedMult;
        float r = radius + (float)(Math.sin(phase * 1.2) * radius * 0.25);
        float x = cx + (float)(Math.cos(phase) * 40);
        float y = cy + (float)(Math.sin(phase * 0.7) * 30);

        int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
        Color center = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
        Color edge = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

        RadialGradientPaint rgp = new RadialGradientPaint(
            x, y, r,
            new float[]{0f, 0.5f, 1f},
            new Color[]{center, new Color(color.getRed(), color.getGreen(), color.getBlue(), a / 3), edge}
        );
        g2.setPaint(rgp);
        g2.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
    }

    public void stopAnimation() {
        if (animTimer != null) animTimer.stop();
    }

    public void startAnimation() {
        if (animTimer != null && !animTimer.isRunning()) animTimer.start();
    }

    // ====== Sparkle particle ======
    private static class Particle {
        float x, y;       // position in percentage (0-100)
        float speed;       // upward speed
        float size;
        float life;        // 1.0 -> 0.0
        float brightness;
        float phase;
        Color color;

        Particle(Random rand) {
            x = rand.nextFloat() * 100;
            y = 80 + rand.nextFloat() * 30; // start from bottom
            speed = 0.15f + rand.nextFloat() * 0.35f;
            size = 2 + rand.nextFloat() * 4;
            life = 0.7f + rand.nextFloat() * 0.3f;
            brightness = 0.3f + rand.nextFloat() * 0.7f;
            phase = rand.nextFloat() * 6.28f;
            Color[] colors = {
                Constants.ACCENT_PRIMARY_LIGHT,
                Constants.ACCENT_SECONDARY,
                Constants.ACCENT_INFO,
                new Color(0xA855F7),
                Color.WHITE
            };
            color = colors[rand.nextInt(colors.length)];
        }
    }
}
