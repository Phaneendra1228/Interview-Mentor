package com.interviewmentor.service;

import com.interviewmentor.model.PerformanceStats;
import com.interviewmentor.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

public class PdfExportService {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final AchievementService achievementService = new AchievementService();

    public boolean exportReport(User user, String destPath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(destPath));
            document.open();

            // Colors
            Color primaryDark = new Color(30, 41, 59); // Slate 800
            Color accentColor = new Color(37, 99, 235); // Blue 600
            Color textDark = new Color(51, 65, 85); // Slate 700
            Color textLight = new Color(241, 245, 249); // Slate 100
            
            // Fonts
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, textLight);
            Font roleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, textLight);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, primaryDark);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, textDark);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, textDark);
            Font skillFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textDark);

            // 1. HEADER (Full width colored background)
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(primaryDark);
            headerCell.setPaddingTop(30f);
            headerCell.setPaddingBottom(30f);
            headerCell.setPaddingLeft(40f);
            headerCell.setBorder(PdfPCell.NO_BORDER);

            Paragraph name = new Paragraph(user.getUsername().toUpperCase(), nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(name);

            String role = user.getTargetRole() != null && !user.getTargetRole().isEmpty() ? user.getTargetRole() : "Software Engineer";
            String exp = user.getExperienceLevel() != null && !user.getExperienceLevel().isEmpty() ? user.getExperienceLevel() : "Professional";
            String email = user.getEmail() != null ? user.getEmail() : "user@example.com";
            
            Paragraph contact = new Paragraph(
                email + "   \u2022   " + role + "   \u2022   " + exp,
                roleFont
            );
            contact.setAlignment(Element.ALIGN_CENTER);
            contact.setSpacingBefore(10f);
            headerCell.addElement(contact);
            headerTable.addCell(headerCell);
            document.add(headerTable);
            document.add(new Paragraph(" "));

            // Helper for Section Titles
            java.util.function.Consumer<String> addSectionTitle = (titleText) -> {
                try {
                    PdfPTable titleTable = new PdfPTable(1);
                    titleTable.setWidthPercentage(100);
                    titleTable.setSpacingBefore(15f);
                    titleTable.setSpacingAfter(10f);
                    
                    PdfPCell cell = new PdfPCell(new Phrase(titleText, sectionFont));
                    cell.setBorder(PdfPCell.BOTTOM);
                    cell.setBorderWidthBottom(2f);
                    cell.setBorderColorBottom(accentColor);
                    cell.setPaddingBottom(5f);
                    titleTable.addCell(cell);
                    document.add(titleTable);
                } catch (Exception e) {}
            };

            // 2. PROFESSIONAL PROFILE
            addSectionTitle.accept("PROFESSIONAL PROFILE");
            String tech = user.getTechStack() != null && !user.getTechStack().isEmpty() ? user.getTechStack() : "modern software development";
            Paragraph profileText = new Paragraph(
                "A dedicated and consistent professional actively refining technical skills in " + tech + " and problem-solving abilities. " +
                "Demonstrates a strong track record of structured interview preparation, algorithmic thinking, and continuous learning " +
                "through rigorous technical assessments.", normalFont);
            profileText.setSpacingBefore(5f);
            document.add(profileText);

            // 3. TECHNICAL COMPETENCIES & PROFICIENCY
            addSectionTitle.accept("TECHNICAL COMPETENCIES");
            List<PerformanceStats> categoryStats = analyticsService.getCategoryStats(user.getId());
            if (categoryStats != null && !categoryStats.isEmpty()) {
                PdfPTable skillsTable = new PdfPTable(2);
                skillsTable.setWidthPercentage(100);
                skillsTable.setSpacingBefore(5f);
                skillsTable.setSpacingAfter(15f);
                
                class ProgressBarEvent implements com.lowagie.text.pdf.PdfPCellEvent {
                    private final float percentage;
                    public ProgressBarEvent(float percentage) { this.percentage = percentage; }
                    @Override
                    public void cellLayout(PdfPCell cell, com.lowagie.text.Rectangle position, com.lowagie.text.pdf.PdfContentByte[] canvases) {
                        com.lowagie.text.pdf.PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
                        cb.saveState();
                        cb.setColorFill(new Color(226, 232, 240)); // Track (Slate 200)
                        cb.roundRectangle(position.getLeft() + 10, position.getBottom() + 4, position.getWidth() - 20, 4, 2);
                        cb.fill();
                        if (percentage > 0) {
                            cb.setColorFill(new Color(37, 99, 235)); // Fill (Blue 600)
                            float fw = (position.getWidth() - 20) * (percentage / 100f);
                            cb.roundRectangle(position.getLeft() + 10, position.getBottom() + 4, fw, 4, 2);
                            cb.fill();
                        }
                        cb.restoreState();
                    }
                }

                for (PerformanceStats stat : categoryStats) {
                    if (stat.getTotalAttempted() > 0) {
                        PdfPCell skillCell = new PdfPCell();
                        skillCell.setBorder(PdfPCell.NO_BORDER);
                        skillCell.setPaddingLeft(10f);
                        skillCell.setPaddingRight(10f);
                        skillCell.setPaddingBottom(16f); // Room for the progress bar
                        
                        PdfPTable inner = new PdfPTable(2);
                        inner.setWidthPercentage(100);
                        PdfPCell left = new PdfPCell(new Phrase(stat.getCategory(), boldFont));
                        left.setBorder(PdfPCell.NO_BORDER);
                        PdfPCell right = new PdfPCell(new Phrase(String.format("%.1f%%", stat.getAccuracy()), skillFont));
                        right.setBorder(PdfPCell.NO_BORDER);
                        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        inner.addCell(left);
                        inner.addCell(right);
                        
                        skillCell.addElement(inner);
                        skillCell.setCellEvent(new ProgressBarEvent((float)stat.getAccuracy()));
                        skillsTable.addCell(skillCell);
                    }
                }
                if (skillsTable.getRows().size() > 0 && skillsTable.getRows().get(skillsTable.getRows().size() - 1).getCells().length < 2) {
                    PdfPCell empty = new PdfPCell();
                    empty.setBorder(PdfPCell.NO_BORDER);
                    skillsTable.addCell(empty);
                }
                document.add(skillsTable);
            } else {
                document.add(new Paragraph("No technical categories evaluated yet.", normalFont));
                document.add(new Paragraph(" "));
            }

            // 4. PERFORMANCE METRICS
            addSectionTitle.accept("PERFORMANCE METRICS");
            PerformanceStats overall = analyticsService.getOverallStats(user.getId());
            int totalQuizzes = analyticsService.getTotalQuizCount(user.getId());
            
            if (overall != null && overall.getTotalAttempted() > 0) {
                PdfPTable metricsTable = new PdfPTable(3);
                metricsTable.setWidthPercentage(100);
                metricsTable.setSpacingBefore(10f);
                metricsTable.setSpacingAfter(15f);

                Font metricValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, accentColor);
                Font metricLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textDark);

                java.util.function.Consumer<String[]> addMetric = (data) -> {
                    PdfPCell cell = new PdfPCell();
                    cell.setBorder(PdfPCell.NO_BORDER);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    Paragraph val = new Paragraph(data[0], metricValueFont);
                    val.setAlignment(Element.ALIGN_CENTER);
                    Paragraph lbl = new Paragraph(data[1], metricLabelFont);
                    lbl.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(val);
                    cell.addElement(lbl);
                    metricsTable.addCell(cell);
                };

                addMetric.accept(new String[]{ String.valueOf(totalQuizzes), "Total Quizzes" });
                addMetric.accept(new String[]{ String.format("%.1f%%", overall.getAccuracy()), "Overall Accuracy" });
                addMetric.accept(new String[]{ String.valueOf(overall.getTotalAttempted()), "Questions Answered" });

                document.add(metricsTable);

                Paragraph perfDetails = new Paragraph(
                    "Demonstrated rapid problem-solving capabilities with an average response time of " + 
                    String.format("%.1f", overall.getAvgTimeSeconds()) + " seconds per technical question.", normalFont);
                document.add(perfDetails);
                document.add(new Paragraph(" "));
            } else {
                document.add(new Paragraph("Performance data is currently being gathered.", normalFont));
                document.add(new Paragraph(" "));
            }

            // 5. HONORS & ACHIEVEMENTS
            addSectionTitle.accept("HONORS & ACHIEVEMENTS");
            List<String> earned = achievementService.getEarnedAchievements(user.getId());
            if (earned != null && !earned.isEmpty()) {
                com.lowagie.text.List achList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
                achList.setListSymbol("\u2022 ");
                for (String achId : earned) {
                    String[] def = AchievementService.getDefinition(achId);
                    if (def != null) {
                        Phrase p = new Phrase();
                        p.add(new com.lowagie.text.Chunk(" " + def[1] + " \u2013 ", boldFont));
                        p.add(new com.lowagie.text.Chunk(def[2], normalFont));
                        achList.add(new com.lowagie.text.ListItem(p));
                    }
                }
                document.add(achList);
            } else {
                document.add(new Paragraph("No achievements earned yet.", normalFont));
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
