package com.interviewmentor.view;

import com.interviewmentor.model.Category;
import com.interviewmentor.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;

/**
 * Learning Resources panel with YouTube videos and study notes per category.
 */
public class ResourcesPanel extends JPanel {

    private final MainFrame mainFrame;
    private JPanel contentArea;
    private Category selectedCategory = Category.DATA_STRUCTURES;

    // YouTube videos: {title, channel, url, duration}
    private static final java.util.Map<String, String[][]> VIDEOS = new java.util.LinkedHashMap<>() {{
        put("DATA_STRUCTURES", new String[][]{
            {"Data Structures Full Course", "Bro Code", "https://www.youtube.com/watch?v=CBYHwZcbD-s", "3:41:00"},
            {"Data Structures Easy to Advanced", "freeCodeCamp", "https://www.youtube.com/watch?v=RBSGKlAvoiM", "8:03:00"},
            {"Data Structures in Java", "Dinesh Varyani", "https://www.youtube.com/playlist?list=PL6Zs6LgrJj3tDXv8a_elC6eT_4R5gfX4d", "Playlist"},
            {"Linked Lists, Stacks, Queues", "Abdul Bari", "https://www.youtube.com/watch?v=YQs6IC-vgmo", "1:12:00"},
            {"Trees & Graphs Explained", "mycodeschool", "https://www.youtube.com/watch?v=oSWTXtMglKE", "20:00"},
        });
        put("ALGORITHMS", new String[][]{
            {"Algorithms Full Course", "freeCodeCamp", "https://www.youtube.com/watch?v=8hly31xKli0", "5:22:00"},
            {"Abdul Bari Algorithms Playlist", "Abdul Bari", "https://www.youtube.com/playlist?list=PLDN4rrl48XKpZkf03iYFl-O29szjTrs_O", "Playlist"},
            {"Dynamic Programming", "Aditya Verma", "https://www.youtube.com/playlist?list=PL_z_8CaSLPWekqhdCPmFohncHwz8TY2Go", "Playlist"},
            {"Sorting Algorithms Visualized", "Bro Code", "https://www.youtube.com/watch?v=HGk_8y2OgXQ", "24:00"},
            {"Graph Algorithms", "William Fiset", "https://www.youtube.com/watch?v=DgXR2OWQnLc", "7:19:00"},
        });
        put("OPERATING_SYSTEMS", new String[][]{
            {"Operating Systems Full Course", "Gate Smashers", "https://www.youtube.com/playlist?list=PLxCzCOWd7aiGz9donHRrE9I3Mwn6XdP8p", "Playlist"},
            {"OS Concepts", "Neso Academy", "https://www.youtube.com/playlist?list=PLBlnK6fEyqRiVhbXDGLXDk_OQAdc0cPiS", "Playlist"},
            {"Process Scheduling Algorithms", "Jenny's Lectures", "https://www.youtube.com/watch?v=EWkQl0n0web", "45:00"},
            {"Deadlocks in OS", "Gate Smashers", "https://www.youtube.com/watch?v=UVo9cGBQRf4", "18:00"},
            {"Memory Management", "Neso Academy", "https://www.youtube.com/watch?v=dz9Tk6KCMlQ", "25:00"},
        });
        put("DBMS", new String[][]{
            {"DBMS Full Course", "Gate Smashers", "https://www.youtube.com/playlist?list=PLxCzCOWd7aiFAN6I8CuViBuCdJgiOkT2Y", "Playlist"},
            {"SQL Tutorial Full Course", "Bro Code", "https://www.youtube.com/watch?v=5OdVJbNCSso", "4:20:00"},
            {"Normalization (1NF to BCNF)", "Gate Smashers", "https://www.youtube.com/watch?v=ABwD8IYBFpQ", "12:00"},
            {"ER Diagrams Tutorial", "Lucid Software", "https://www.youtube.com/watch?v=QpdhBUYk7Kk", "12:00"},
            {"ACID Properties & Transactions", "Jenny's Lectures", "https://www.youtube.com/watch?v=GAe5oB742dw", "25:00"},
        });
        put("JAVA", new String[][]{
            {"Java Full Course for Beginners", "Bro Code", "https://www.youtube.com/watch?v=xk4_1vDrzzo", "12:00:00"},
            {"Java OOP Concepts", "Telusko", "https://www.youtube.com/playlist?list=PLsyeobzWxl7oZ-fxDYkOToURHhMuWD1BK", "Playlist"},
            {"Java Collections Framework", "Telusko", "https://www.youtube.com/watch?v=rzA7UJ-hQn4", "2:30:00"},
            {"Multithreading in Java", "Smart Programming", "https://www.youtube.com/watch?v=KuvkahVyY9E", "1:45:00"},
            {"Java Streams API", "Amigoscode", "https://www.youtube.com/watch?v=Q93JsQ8vcwY", "50:00"},
        });
        put("PYTHON", new String[][]{
            {"Python Full Course", "Bro Code", "https://www.youtube.com/watch?v=XKHEtdqhLK8", "12:00:00"},
            {"Python for Beginners", "Programming with Mosh", "https://www.youtube.com/watch?v=_uQrJ0TkZlc", "6:14:00"},
            {"Python OOP", "Corey Schafer", "https://www.youtube.com/playlist?list=PL-osiE80TeTsqhIuOqKhwlXsIBIdSeYtc", "Playlist"},
            {"Decorators & Generators", "Corey Schafer", "https://www.youtube.com/watch?v=FsAPt_9Bf3U", "30:00"},
            {"Python Data Structures", "freeCodeCamp", "https://www.youtube.com/watch?v=pkYVOmU3MgA", "12:30:00"},
        });
        put("OOP", new String[][]{
            {"OOP in 7 Minutes", "Mosh Hamedani", "https://www.youtube.com/watch?v=pTB0EiLXUC8", "7:00"},
            {"OOP Concepts Explained", "freeCodeCamp", "https://www.youtube.com/watch?v=SiBw7os-_zI", "46:00"},
            {"SOLID Principles", "freeCodeCamp", "https://www.youtube.com/watch?v=_jDNAf3CzeY", "1:03:00"},
            {"Design Patterns in Java", "Derek Banas", "https://www.youtube.com/playlist?list=PLF206E906175C7E07", "Playlist"},
            {"Abstraction vs Encapsulation", "Telusko", "https://www.youtube.com/watch?v=1Q8yKATOm4A", "10:00"},
        });
        put("COMPUTER_NETWORKS", new String[][]{
            {"Computer Networks Full Course", "Gate Smashers", "https://www.youtube.com/playlist?list=PLxCzCOWd7aiGFBD2-2joCpWOLUrDLvVV_", "Playlist"},
            {"Networking Fundamentals", "NetworkChuck", "https://www.youtube.com/watch?v=qiQR5rTSshw", "28:00"},
            {"TCP/IP Model", "Neso Academy", "https://www.youtube.com/watch?v=OTwp3xtd4dg", "15:00"},
            {"HTTP, DNS, DHCP Explained", "PowerCert Animated", "https://www.youtube.com/watch?v=e4S8zfLdLgQ", "18:00"},
            {"OSI Model Explained", "TechTerms", "https://www.youtube.com/watch?v=LANW3m7UgJI", "5:00"},
        });
        put("BEHAVIORAL", new String[][]{
            {"Master the Behavioral Interview", "Jeff Su", "https://www.youtube.com/watch?v=1mHjMNZZvFo", "15:00"},
            {"STAR Method Explained", "CareerVidz", "https://www.youtube.com/watch?v=Wrl-4I5sXFA", "18:30"},
            {"Top 10 Behavioral Interview Questions", "Dan Croitor", "https://www.youtube.com/playlist?list=PLp0B5N8T1cCOmD9iQz8u5G4hE0gQf_z_H", "Playlist"},
            {"Amazon Leadership Principles", "Dan Croitor", "https://www.youtube.com/watch?v=B-WcsA2S5qI", "20:00"},
            {"Conflict Resolution at Work", "Harvard Business Review", "https://www.youtube.com/watch?v=KY5TWVz5Z9Q", "8:45"},
        });
    }};

    // PDF Notes: {title, source, url}
    private static final java.util.Map<String, String[][]> PDFS = new java.util.LinkedHashMap<>() {{
        put("DATA_STRUCTURES", new String[][]{
            {"Data Structures & Algorithms Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/data-structures/"},
            {"DSA Complete Notes PDF", "Tutorialspoint", "https://www.tutorialspoint.com/data_structures_algorithms/data_structures_algorithms_tutorial.pdf"},
            {"Stacks, Queues & Trees Handbook", "Open DSA", "https://opendsa-server.cs.vt.edu/OpenDSA/Books/Everything/html/"},
            {"MIT OpenCourseWare — DSA", "MIT OCW", "https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/"},
        });
        put("ALGORITHMS", new String[][]{
            {"Algorithm Design Manual Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/fundamentals-of-algorithms/"},
            {"Algorithms Notes for Professionals", "GoalKicker", "https://goalkicker.com/AlgorithmsBook/AlgorithmsNotesForProfessionals.pdf"},
            {"Sorting & Searching Cheat Sheet", "BigOCheatSheet", "https://www.bigocheatsheet.com/"},
            {"Dynamic Programming Guide", "CP-Algorithms", "https://cp-algorithms.com/"},
        });
        put("OPERATING_SYSTEMS", new String[][]{
            {"Operating Systems Complete Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/operating-systems/"},
            {"OS Notes for Professionals", "GoalKicker", "https://goalkicker.com/OperatingSystemsBook/"},
            {"OS Concepts — Galvin Textbook Notes", "Tutorialspoint", "https://www.tutorialspoint.com/operating_system/operating_system_tutorial.pdf"},
            {"Process & Memory Management", "JavaTpoint", "https://www.javatpoint.com/os-tutorial"},
        });
        put("DBMS", new String[][]{
            {"DBMS Complete Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/dbms/"},
            {"SQL Notes for Professionals", "GoalKicker", "https://goalkicker.com/SQLBook/SQLNotesForProfessionals.pdf"},
            {"Database Normalization Guide", "Tutorialspoint", "https://www.tutorialspoint.com/dbms/dbms_tutorial.pdf"},
            {"SQL & DBMS Interview Questions", "JavaTpoint", "https://www.javatpoint.com/dbms-tutorial"},
        });
        put("JAVA", new String[][]{
            {"Java Notes for Professionals", "GoalKicker", "https://goalkicker.com/JavaBook/JavaNotesForProfessionals.pdf"},
            {"Java Complete Reference Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/java/"},
            {"Java Collections Framework", "Oracle Docs", "https://docs.oracle.com/javase/tutorial/collections/index.html"},
            {"Java Multithreading Guide", "Tutorialspoint", "https://www.tutorialspoint.com/java/java_multithreading.htm"},
        });
        put("PYTHON", new String[][]{
            {"Python Notes for Professionals", "GoalKicker", "https://goalkicker.com/PythonBook/PythonNotesForProfessionals.pdf"},
            {"Python Complete Tutorial", "GeeksforGeeks", "https://www.geeksforgeeks.org/python-programming-language-tutorial/"},
            {"Python Official Documentation", "Python.org", "https://docs.python.org/3/tutorial/index.html"},
            {"Python OOP & Advanced Topics", "Real Python", "https://realpython.com/python3-object-oriented-programming/"},
        });
        put("OOP", new String[][]{
            {"OOP Concepts Complete Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/object-oriented-programming-oops-concept-in-java/"},
            {"SOLID Principles Explained", "DigitalOcean", "https://www.digitalocean.com/community/conceptual-articles/s-o-l-i-d-the-first-five-principles-of-object-oriented-design"},
            {"Design Patterns — Refactoring Guru", "Refactoring Guru", "https://refactoring.guru/design-patterns/catalog"},
            {"OOP in Java & Python", "Tutorialspoint", "https://www.tutorialspoint.com/object_oriented_analysis_design/ooad_object_oriented_paradigm.htm"},
        });
        put("COMPUTER_NETWORKS", new String[][]{
            {"Computer Networks Notes", "GeeksforGeeks", "https://www.geeksforgeeks.org/computer-network-tutorials/"},
            {"Networking Notes for Professionals", "GoalKicker", "https://goalkicker.com/"},
            {"TCP/IP & OSI Model Guide", "Tutorialspoint", "https://www.tutorialspoint.com/data_communication_computer_network/data_communication_computer_network_tutorial.pdf"},
            {"HTTP & DNS Explained", "MDN Web Docs", "https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview"},
        });
        put("BEHAVIORAL", new String[][]{
            {"STAR Method Cheat Sheet", "The Muse", "https://www.themuse.com/advice/star-interview-method"},
            {"Behavioral Interview Questions Guide", "Indeed", "https://www.indeed.com/career-advice/interviewing/how-to-prepare-for-a-behavioral-interview"},
            {"Amazon Leadership Principles Guide", "Amazon Jobs", "https://www.amazon.jobs/content/en/our-workplace/leadership-principles"},
            {"Top 30 Behavioral Questions PDF", "Zety", "https://zety.com/blog/behavioral-interview-questions"},
        });
    }};

    public ResourcesPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Category tabs
        JPanel tabBar = new JPanel();
        tabBar.setOpaque(false);
        tabBar.setLayout(new GridLayout(2, 4, 10, 10));
        tabBar.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));
        for (Category cat : Category.values()) {
            JButton tab = new JButton(cat.getShortCode()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active = cat == selectedCategory;
                    g2.setColor(active ? Constants.withAlpha(cat.getColor(), 35) : Constants.BG_CARD);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    if (active) {
                        g2.setColor(cat.getColor());
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                    }
                    g2.setColor(active ? cat.getColor() : Constants.TEXT_SECONDARY);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(cat.getDisplayName(), (getWidth()-fm.stringWidth(cat.getDisplayName()))/2, getHeight()/2+fm.getAscent()/3);
                    g2.dispose();
                }
            };
            tab.setOpaque(false); tab.setContentAreaFilled(false);
            tab.setBorderPainted(false); tab.setFocusPainted(false);
            tab.setFont(Constants.FONT_SMALL);
            tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tab.setPreferredSize(new Dimension(120, 34));
            tab.addActionListener(e -> { selectedCategory = cat; refreshContent(); tabBar.repaint(); });
            tabBar.add(tab);
        }
        wrapper.add(tabBar, BorderLayout.NORTH);

        contentArea = new JPanel();
        contentArea.setOpaque(false);
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBorder(BorderFactory.createEmptyBorder(16, 36, 28, 36));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Constants.BG_PRIMARY);
        scroll.getViewport().setBackground(Constants.BG_PRIMARY);
        wrapper.add(scroll, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        refreshContent();
    }

    public void refresh() { refreshContent(); }

    private void refreshContent() {
        contentArea.removeAll();
        Category cat = selectedCategory;

        // Header
        JLabel title = new JLabel(cat.getDisplayName() + " — Learning Resources");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(title);
        contentArea.add(Box.createVerticalStrut(6));
        JLabel desc = new JLabel(cat.getDescription());
        desc.setFont(Constants.FONT_BODY);
        desc.setForeground(Constants.TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(desc);
        contentArea.add(Box.createVerticalStrut(24));

        // YouTube section
        Icon ytIcon = new Icon() {
            @Override public int getIconWidth() { return 28; }
            @Override public int getIconHeight() { return 20; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF0000));
                g2.fill(new RoundRectangle2D.Float(x, y + 2, 28, 20, 8, 8));
                g2.setColor(Color.WHITE);
                java.awt.geom.Path2D.Float triangle = new java.awt.geom.Path2D.Float();
                triangle.moveTo(x + 10, y + 7);
                triangle.lineTo(x + 19, y + 12);
                triangle.lineTo(x + 10, y + 17);
                triangle.closePath();
                g2.fill(triangle);
                g2.dispose();
            }
        };

        JLabel videoTitle = new JLabel("  YouTube Videos", ytIcon, SwingConstants.LEFT);
        videoTitle.setFont(Constants.FONT_HEADING);
        videoTitle.setForeground(Constants.TEXT_PRIMARY);
        videoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(videoTitle);
        contentArea.add(Box.createVerticalStrut(12));

        String[][] videos = VIDEOS.getOrDefault(cat.name(), new String[0][]);
        JPanel videoGrid = new JPanel(new GridLayout(0, 2, 14, 14));
        videoGrid.setOpaque(false);
        videoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        videoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, videos.length * 55));

        for (String[] vid : videos) {
            videoGrid.add(createVideoCard(vid[0], vid[1], vid[2], vid[3], cat.getColor()));
        }
        if (videos.length % 2 != 0) {
            JPanel spacer = new JPanel(); spacer.setOpaque(false);
            videoGrid.add(spacer);
        }
        contentArea.add(videoGrid);
        contentArea.add(Box.createVerticalStrut(32));

        // PDF section
        JLabel pdfTitle = new JLabel("PDF Notes & Guides");
        pdfTitle.setFont(Constants.FONT_HEADING);
        pdfTitle.setForeground(Constants.ACCENT_INFO);
        pdfTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(pdfTitle);
        contentArea.add(Box.createVerticalStrut(12));

        String[][] pdfs = PDFS.getOrDefault(cat.name(), new String[0][]);
        for (String[] pdf : pdfs) {
            contentArea.add(createPdfCard(pdf[0], pdf[1], pdf[2], cat.getColor()));
            contentArea.add(Box.createVerticalStrut(8));
        }

        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel createVideoCard(String title, String channel, String url, String duration, Color accent) {
        JPanel card = new JPanel() {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov=true; setCursor(new Cursor(Cursor.HAND_CURSOR)); repaint(); }
                @Override public void mouseExited(MouseEvent e) { hov=false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { openURL(url); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Constants.BG_CARD_HOVER : Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Red play indicator
                g2.setColor(hov ? new Color(0xFF0000) : Constants.withAlpha(new Color(0xFF0000), 180));
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Play icon
        Icon cardYtIcon = new Icon() {
            @Override public int getIconWidth() { return 36; }
            @Override public int getIconHeight() { return 26; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF0000));
                g2.fill(new RoundRectangle2D.Float(x, y, 36, 26, 10, 10));
                g2.setColor(Color.WHITE);
                java.awt.geom.Path2D.Float triangle = new java.awt.geom.Path2D.Float();
                triangle.moveTo(x + 13, y + 7);
                triangle.lineTo(x + 24, y + 13);
                triangle.lineTo(x + 13, y + 19);
                triangle.closePath();
                g2.fill(triangle);
                g2.dispose();
            }
        };

        JLabel playIcon = new JLabel(cardYtIcon);
        playIcon.setPreferredSize(new Dimension(40, 30));
        card.add(playIcon, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Constants.FONT_BODY_BOLD);
        titleLbl.setForeground(Constants.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(titleLbl);
        JLabel channelLbl = new JLabel(channel + "  •  " + duration);
        channelLbl.setFont(Constants.FONT_SMALL);
        channelLbl.setForeground(Constants.TEXT_MUTED);
        channelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(channelLbl);
        card.add(info, BorderLayout.CENTER);

        JLabel arrow = new JLabel("->");
        arrow.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
        arrow.setForeground(Constants.TEXT_MUTED);
        card.add(arrow, BorderLayout.EAST);

        return card;
    }

    private JPanel createPdfCard(String title, String source, String url, Color accent) {
        JPanel card = new JPanel() {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov=true; setCursor(new Cursor(Cursor.HAND_CURSOR)); repaint(); }
                @Override public void mouseExited(MouseEvent e) { hov=false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { openURL(url); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Constants.BG_CARD_HOVER : Constants.withAlpha(accent, 10));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Constants.withAlpha(accent, 60));
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel icon = new JLabel("PDF");
        icon.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 11));
        icon.setForeground(accent);
        icon.setPreferredSize(new Dimension(30, 30));
        card.add(icon, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Constants.FONT_BODY_BOLD);
        titleLbl.setForeground(Constants.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(titleLbl);
        JLabel sourceLbl = new JLabel(source);
        sourceLbl.setFont(Constants.FONT_SMALL);
        sourceLbl.setForeground(Constants.TEXT_MUTED);
        sourceLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(sourceLbl);
        card.add(info, BorderLayout.CENTER);

        JLabel arrow = new JLabel("DL");
        arrow.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 11));
        arrow.setForeground(accent);
        card.add(arrow, BorderLayout.EAST);

        return card;
    }

    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Could not open browser:\n" + url,
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
