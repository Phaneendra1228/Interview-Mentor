package com.interviewmentor.util;

import java.awt.*;

/**
 * Application-wide constants: colors, fonts, dimensions, panel names, animation timings.
 */
public final class Constants {

    private Constants() {} // prevent instantiation

    // ====== COLORS ======
    public static final Color BG_DARK       = new Color(0x0F0F1A);
    public static final Color BG_PRIMARY    = new Color(0x1A1A2E);
    public static final Color BG_SECONDARY  = new Color(0x16213E);
    public static final Color BG_CARD       = new Color(0x1E293B);
    public static final Color BG_CARD_HOVER = new Color(0x273549);
    public static final Color BG_SIDEBAR    = new Color(0x0F172A);
    public static final Color BG_INPUT      = new Color(0x1E293B);
    public static final Color BG_HEADER     = new Color(0x0D1320);

    public static final Color ACCENT_PRIMARY       = new Color(0x7C3AED);
    public static final Color ACCENT_PRIMARY_LIGHT = new Color(0x8B5CF6);
    public static final Color ACCENT_SECONDARY     = new Color(0x06B6D4);
    public static final Color ACCENT_SUCCESS       = new Color(0x10B981);
    public static final Color ACCENT_WARNING       = new Color(0xF59E0B);
    public static final Color ACCENT_DANGER        = new Color(0xEF4444);
    public static final Color ACCENT_INFO          = new Color(0x3B82F6);
    public static final Color ACCENT_PINK          = new Color(0xEC4899);
    public static final Color ACCENT_ORANGE        = new Color(0xF97316);

    public static final Color TEXT_PRIMARY   = new Color(0xF1F5F9);
    public static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    public static final Color TEXT_MUTED     = new Color(0x64748B);
    public static final Color BORDER_COLOR   = new Color(0x334155);

    // Glass/Glow effects
    public static final Color GLASS_BG       = new Color(15, 23, 42, 180);
    public static final Color GLASS_BORDER   = new Color(51, 65, 85, 100);
    public static final Color GLOW_PRIMARY   = new Color(124, 58, 237, 60);
    public static final Color GLOW_SUCCESS   = new Color(16, 185, 129, 60);

    // Gradient pair
    public static final Color GRADIENT_START = new Color(0x7C3AED);
    public static final Color GRADIENT_END   = new Color(0x2563EB);

    // Streak colors (heatmap intensity)
    public static final Color STREAK_NONE    = new Color(0x1E293B);
    public static final Color STREAK_LOW     = new Color(0x1E3A5F);
    public static final Color STREAK_MED     = new Color(0x2563EB);
    public static final Color STREAK_HIGH    = new Color(0x7C3AED);
    public static final Color STREAK_MAX     = new Color(0xA855F7);

    // ====== FONTS ======
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_TITLE      = new Font(FONT_FAMILY, Font.BOLD, 32);
    public static final Font FONT_HEADING    = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SUBHEADING = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY       = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD  = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL      = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_TINY       = new Font(FONT_FAMILY, Font.PLAIN, 10);
    public static final Font FONT_BUTTON     = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_LARGE_NUM  = new Font(FONT_FAMILY, Font.BOLD, 36);
    public static final Font FONT_MONO       = new Font("Consolas", Font.BOLD, 18);

    // ====== DIMENSIONS ======
    public static final int SIDEBAR_WIDTH  = 210;
    public static final int HEADER_HEIGHT  = 56;
    public static final int WINDOW_WIDTH   = 1280;
    public static final int WINDOW_HEIGHT  = 820;
    public static final int CARD_RADIUS    = 16;
    public static final int BUTTON_RADIUS  = 12;
    public static final int CARD_PADDING   = 24;

    // ====== ANIMATION TIMINGS (ms) ======
    public static final int ANIM_FAST      = 150;
    public static final int ANIM_NORMAL    = 300;
    public static final int ANIM_SLOW      = 500;
    public static final int TOAST_DURATION = 3000;
    public static final int RIPPLE_DURATION = 400;
    public static final int TRANSITION_DURATION = 350;

    // ====== QUIZ DEFAULTS ======
    public static final int DEFAULT_QUESTION_COUNT = 10;
    public static final int TIME_PER_QUESTION      = 60;

    // ====== PANEL NAMES ======
    public static final String PANEL_LOGIN       = "login";
    public static final String PANEL_DASHBOARD   = "dashboard";
    public static final String PANEL_CATEGORIES  = "categories";
    public static final String PANEL_QUIZ        = "quiz";
    public static final String PANEL_RESULT      = "result";
    public static final String PANEL_PERFORMANCE = "performance";
    public static final String PANEL_HISTORY     = "history";
    public static final String PANEL_BOOKMARKS   = "bookmarks";
    public static final String PANEL_ACHIEVEMENTS = "achievements";
    public static final String PANEL_PROFILE     = "profile";
    public static final String PANEL_RESOURCES   = "resources";
    public static final String PANEL_SIMULATION  = "simulation";
    public static final String PANEL_FLASHCARDS  = "flashcards";
    public static final String PANEL_REVIEW      = "review";
    public static final String PANEL_SETTINGS    = "settings";
    public static final String PANEL_ANALYTICS   = "analytics";
    public static final String PANEL_COUNTDOWN   = "countdown";
    public static final String PANEL_BEHAVIORAL  = "behavioral";

    // ====== MOTIVATIONAL QUOTES ======
    public static final String[] QUOTES = {
        "\"The only way to do great work is to love what you do.\" — Steve Jobs",
        "\"It's not about being the best. It's about being better than you were yesterday.\"",
        "\"Success is the sum of small efforts, repeated day in and day out.\" — Robert Collier",
        "\"The expert in anything was once a beginner.\" — Helen Hayes",
        "\"Don't watch the clock; do what it does. Keep going.\" — Sam Levenson",
        "\"Code is like humor. When you have to explain it, it's bad.\" — Cory House",
        "\"First, solve the problem. Then, write the code.\" — John Johnson",
        "\"Practice makes progress, not perfection.\"",
        "\"Every master was once a disaster.\"",
        "\"The best time to plant a tree was 20 years ago. The second best time is now.\""
    };

    // ====== UTILITY METHODS ======
    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return seconds + "s";
    }

    public static String formatPercentage(double value) {
        return String.format("%.1f%%", value);
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Lerp between two colors by factor t (0.0 to 1.0) */
    public static Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    /** Get today's motivational quote (changes daily) */
    public static String getDailyQuote() {
        int dayOfYear = java.time.LocalDate.now().getDayOfYear();
        return QUOTES[dayOfYear % QUOTES.length];
    }
}
