package com.interviewmentor.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Celebration confetti animation overlay.
 * Shows colorful confetti particles raining down when triggered.
 */
public class ConfettiAnimation extends JPanel {

    private final List<ConfettiPiece> pieces = new ArrayList<>();
    private Timer animTimer;
    private final Random rand = new Random();
    private Runnable onComplete;

    private static final Color[] CONFETTI_COLORS = {
        new Color(0x7C3AED), new Color(0x2563EB), new Color(0x10B981),
        new Color(0xF59E0B), new Color(0xEF4444), new Color(0xEC4899),
        new Color(0x06B6D4), new Color(0xF97316), new Color(0xA855F7),
        new Color(0x22D3EE), Color.WHITE
    };

    public ConfettiAnimation() {
        setOpaque(false);
    }

    /** Trigger the confetti burst */
    public void start(int numPieces, Runnable onCompleteCallback) {
        this.onComplete = onCompleteCallback;
        pieces.clear();
        int w = getWidth() > 0 ? getWidth() : 800;
        for (int i = 0; i < numPieces; i++) {
            pieces.add(new ConfettiPiece(rand, w));
        }
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(20, e -> {
            updatePieces();
            repaint();
            if (pieces.isEmpty()) {
                animTimer.stop();
                if (this.onComplete != null) this.onComplete.run();
            }
        });
        animTimer.start();
    }

    private void updatePieces() {
        int h = getHeight() > 0 ? getHeight() : 800;
        Iterator<ConfettiPiece> it = pieces.iterator();
        while (it.hasNext()) {
            ConfettiPiece p = it.next();
            p.y += p.vy;
            p.x += p.vx + Math.sin(p.wobble) * 1.5f;
            p.wobble += p.wobbleSpeed;
            p.rotation += p.rotSpeed;
            p.vy += 0.12f; // gravity
            p.opacity -= 0.003f;
            if (p.y > h + 20 || p.opacity <= 0) {
                it.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (pieces.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (ConfettiPiece p : pieces) {
            float alpha = Math.max(0, Math.min(1, p.opacity));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(p.color);

            Graphics2D g3 = (Graphics2D) g2.create();
            g3.translate(p.x, p.y);
            g3.rotate(p.rotation);

            switch (p.shape) {
                case 0 -> g3.fill(new Ellipse2D.Float(-p.size / 2, -p.size / 2, p.size, p.size));
                case 1 -> g3.fillRect((int)(-p.size / 2), (int)(-p.size / 3), (int)p.size, (int)(p.size / 1.5f));
                case 2 -> {
                    int[] xp = {0, (int)(p.size / 2), (int)(-p.size / 2)};
                    int[] yp = {(int)(-p.size / 2), (int)(p.size / 3), (int)(p.size / 3)};
                    g3.fillPolygon(xp, yp, 3);
                }
                default -> g3.fillRect((int)(-p.size / 4), (int)(-p.size / 2), (int)(p.size / 2), (int)p.size);
            }
            g3.dispose();
        }
        g2.dispose();
    }

    public void stop() {
        if (animTimer != null) animTimer.stop();
        pieces.clear();
        repaint();
    }

    private static class ConfettiPiece {
        float x, y, vx, vy;
        float size, rotation, rotSpeed;
        float wobble, wobbleSpeed;
        float opacity;
        int shape;
        Color color;

        ConfettiPiece(Random rand, int width) {
            x = rand.nextFloat() * width;
            y = -10 - rand.nextFloat() * 100;
            vx = (rand.nextFloat() - 0.5f) * 4;
            vy = 1 + rand.nextFloat() * 3;
            size = 6 + rand.nextFloat() * 8;
            rotation = rand.nextFloat() * 6.28f;
            rotSpeed = (rand.nextFloat() - 0.5f) * 0.2f;
            wobble = rand.nextFloat() * 6.28f;
            wobbleSpeed = 0.03f + rand.nextFloat() * 0.08f;
            opacity = 0.8f + rand.nextFloat() * 0.2f;
            shape = rand.nextInt(4);
            color = CONFETTI_COLORS[rand.nextInt(CONFETTI_COLORS.length)];
        }
    }
}
