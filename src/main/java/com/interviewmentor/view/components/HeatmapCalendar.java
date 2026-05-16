package com.interviewmentor.view.components;

import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GitHub-style activity heatmap calendar component.
 * Shows daily activity intensity over the last 16 weeks.
 */
public class HeatmapCalendar extends JPanel {

    private Map<String, Integer> activityData = new HashMap<>();  // date -> count
    private int maxActivity = 1;

    public HeatmapCalendar() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 130));
    }

    /** Set activity data map: key = "YYYY-MM-DD", value = quiz count */
    public void setData(Map<String, Integer> data) {
        this.activityData = data != null ? data : new HashMap<>();
        this.maxActivity = activityData.values().stream().mapToInt(v -> v).max().orElse(1);
        if (maxActivity == 0) maxActivity = 1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cellSize = 13, gap = 3, totalCell = cellSize + gap;
        int weeks = 16;
        int leftMargin = 30;
        int topMargin = 20;

        // Day labels
        g2.setFont(Constants.FONT_TINY);
        g2.setColor(Constants.TEXT_MUTED);
        String[] days = {"", "Mon", "", "Wed", "", "Fri", ""};
        for (int d = 0; d < 7; d++) {
            if (!days[d].isEmpty()) {
                g2.drawString(days[d], 2, topMargin + d * totalCell + cellSize - 2);
            }
        }

        // Draw cells
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(weeks - 1);
        // Align to Monday
        while (startDate.getDayOfWeek().getValue() != 1) startDate = startDate.minusDays(1);

        LocalDate current = startDate;
        int weekIdx = 0;
        String lastMonth = "";

        while (!current.isAfter(today)) {
            int dow = current.getDayOfWeek().getValue() - 1; // 0=Mon

            // Month label
            String month = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (!month.equals(lastMonth) && dow == 0) {
                g2.setColor(Constants.TEXT_MUTED);
                g2.setFont(Constants.FONT_TINY);
                g2.drawString(month, leftMargin + weekIdx * totalCell, topMargin - 6);
                lastMonth = month;
            }

            int x = leftMargin + weekIdx * totalCell;
            int y = topMargin + dow * totalCell;

            String dateKey = current.toString();
            int count = activityData.getOrDefault(dateKey, 0);
            Color cellColor = getIntensityColor(count);

            g2.setColor(cellColor);
            g2.fill(new RoundRectangle2D.Float(x, y, cellSize, cellSize, 3, 3));

            // Advance
            current = current.plusDays(1);
            if (current.getDayOfWeek().getValue() == 1) weekIdx++;
        }

        // Legend
        int legendX = leftMargin;
        int legendY = topMargin + 7 * totalCell + 8;
        g2.setColor(Constants.TEXT_MUTED);
        g2.setFont(Constants.FONT_TINY);
        g2.drawString("Less", legendX, legendY + cellSize - 2);
        legendX += 28;
        for (int i = 0; i <= 4; i++) {
            g2.setColor(getIntensityColor(i * maxActivity / 4));
            g2.fill(new RoundRectangle2D.Float(legendX + i * (cellSize + 2), legendY, cellSize, cellSize, 3, 3));
        }
        legendX += 5 * (cellSize + 2) + 4;
        g2.setColor(Constants.TEXT_MUTED);
        g2.drawString("More", legendX, legendY + cellSize - 2);

        g2.dispose();
    }

    private Color getIntensityColor(int count) {
        if (count == 0) return Constants.STREAK_NONE;
        float ratio = (float) count / maxActivity;
        if (ratio <= 0.25f) return Constants.STREAK_LOW;
        if (ratio <= 0.50f) return Constants.STREAK_MED;
        if (ratio <= 0.75f) return Constants.STREAK_HIGH;
        return Constants.STREAK_MAX;
    }
}
