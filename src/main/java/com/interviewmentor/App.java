package com.interviewmentor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.interviewmentor.service.DatabaseService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InterviewMentor — Technical Interview Preparation Trainer
 * Main application entry point with animated splash screen.
 */
public class App {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
        } catch (Exception e) {
            System.err.println("Failed to setup FlatLaf: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            JWindow splash = new JWindow();
            splash.setSize(460, 240);
            splash.setLocationRelativeTo(null);
            SplashPanel splashPanel = new SplashPanel();
            splash.setContentPane(splashPanel);
            splash.setVisible(true);

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    DatabaseService.getInstance().initialize();
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                    return null;
                }
                @Override protected void done() {
                    splashPanel.stopAnim();
                    splash.dispose();
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                }
            }.execute();
        });
    }

    static class SplashPanel extends JPanel {
        float phase = 0f;
        Timer anim;
        SplashPanel() {
            anim = new Timer(30, e -> { phase += 0.03f; repaint(); });
            anim.start();
        }
        void stopAnim() { if (anim != null) anim.stop(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, Constants.BG_DARK, w, h, new Color(0x0D0D2B)));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
            float ox = w * 0.7f + (float)(Math.cos(phase) * 20);
            float oy = h * 0.3f + (float)(Math.sin(phase) * 15);
            g2.setPaint(new RadialGradientPaint(ox, oy, 100, new float[]{0f, 1f},
                new Color[]{Constants.withAlpha(Constants.ACCENT_PRIMARY, 40), new Color(0,0,0,0)}));
            g2.fillOval((int)(ox-100),(int)(oy-100),200,200);
            g2.setColor(Constants.withAlpha(Constants.ACCENT_PRIMARY, 80));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1,1,w-2,h-2,20,20));
            int ls=50, lx=(w-ls)/2, ly=40;
            g2.setPaint(new GradientPaint(lx,ly,Constants.ACCENT_PRIMARY,lx+ls,ly+ls,Constants.GRADIENT_END));
            g2.fill(new RoundRectangle2D.Float(lx,ly,ls,ls,14,14));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Constants.FONT_FAMILY,Font.BOLD,20));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString("IM",lx+(ls-fm.stringWidth("IM"))/2,ly+ls/2+fm.getAscent()/3);
            g2.setColor(Constants.TEXT_PRIMARY);
            g2.setFont(new Font(Constants.FONT_FAMILY,Font.BOLD,22));
            fm=g2.getFontMetrics();
            String t="InterviewMentor";
            g2.drawString(t,(w-fm.stringWidth(t))/2,ly+ls+30);
            g2.setColor(Constants.TEXT_MUTED); g2.setFont(Constants.FONT_SMALL);
            fm=g2.getFontMetrics();
            String s="Technical Interview Preparation Trainer";
            g2.drawString(s,(w-fm.stringWidth(s))/2,ly+ls+48);
            int by=h-50,bw=w-80,bh=6,bx=40;
            g2.setColor(Constants.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(bx,by,bw,bh,3,3));
            float bp=(phase*2)%2f;
            int fs=(int)(bw*Math.max(0,bp-0.3f)), fe=(int)(bw*Math.min(1,bp));
            if(fe>fs){g2.setPaint(new GradientPaint(bx+fs,by,Constants.ACCENT_PRIMARY,bx+fe,by,Constants.GRADIENT_END));
            g2.fill(new RoundRectangle2D.Float(bx+fs,by,fe-fs,bh,3,3));}
            g2.setColor(Constants.TEXT_MUTED); g2.setFont(Constants.FONT_TINY);
            fm=g2.getFontMetrics();
            String lt="Loading resources...";
            g2.drawString(lt,(w-fm.stringWidth(lt))/2,by+bh+16);
            g2.drawString("v1.0",w-40,h-10);
            g2.dispose();
        }
    }
}
