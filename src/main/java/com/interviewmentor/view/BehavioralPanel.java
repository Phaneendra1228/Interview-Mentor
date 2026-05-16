package com.interviewmentor.view;

import com.interviewmentor.model.StarStory;
import com.interviewmentor.model.User;
import com.interviewmentor.service.StarStoryService;
import com.interviewmentor.util.Constants;
import com.interviewmentor.view.components.RoundedButton;
import com.interviewmentor.view.components.ToastNotification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class BehavioralPanel extends JPanel {

    private final MainFrame mainFrame;
    private final StarStoryService starService = new StarStoryService();

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private JPanel listPanel;
    private JPanel editorPanel;
    private JPanel storiesContainer;

    // Editor fields
    private StarStory currentStory;
    private JComboBox<String> questionCombo;
    private JTextArea situationArea;
    private JTextArea taskArea;
    private JTextArea actionArea;
    private JTextArea resultArea;

    private static final String[] COMMON_QUESTIONS = {
        "Tell me about a time you faced a difficult challenge at work.",
        "Describe a situation where you had to meet a tight deadline.",
        "Tell me about a time you disagreed with a team member or manager.",
        "Describe a time you showed leadership initiative.",
        "Tell me about a time you made a mistake and how you handled it.",
        "Describe a project you are most proud of.",
        "Tell me about a time you had to learn a new technology quickly.",
        "Custom Question (Type below)"
    };

    public BehavioralPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Constants.BG_PRIMARY);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        buildListPanel();
        buildEditorPanel();

        cardsPanel.add(listPanel, "LIST");
        cardsPanel.add(editorPanel, "EDITOR");
        add(cardsPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        showList();
    }

    private void buildListPanel() {
        listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 36, 16, 36));

        JLabel title = new JLabel("Behavioral Stories (STAR Method)");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        RoundedButton newBtn = new RoundedButton("+ New Story");
        newBtn.setPreferredSize(new Dimension(140, 40));
        newBtn.addActionListener(e -> openEditor(null));
        header.add(newBtn, BorderLayout.EAST);

        listPanel.add(header, BorderLayout.NORTH);

        storiesContainer = new JPanel();
        storiesContainer.setLayout(new BoxLayout(storiesContainer, BoxLayout.Y_AXIS));
        storiesContainer.setOpaque(false);
        storiesContainer.setBorder(new EmptyBorder(0, 36, 28, 36));

        JScrollPane scroll = new JScrollPane(storiesContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        listPanel.add(scroll, BorderLayout.CENTER);
    }

    private void showList() {
        storiesContainer.removeAll();
        User user = mainFrame.getCurrentUser();
        if (user != null) {
            List<StarStory> stories = starService.getStoriesForUser(user.getId());
            if (stories.isEmpty()) {
                JLabel empty = new JLabel("No stories saved. Click '+ New Story' to start building your behavioral answers.");
                empty.setFont(Constants.FONT_BODY);
                empty.setForeground(Constants.TEXT_MUTED);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                storiesContainer.add(empty);
            } else {
                for (StarStory s : stories) {
                    storiesContainer.add(createStoryCard(s));
                    storiesContainer.add(Box.createVerticalStrut(12));
                }
            }
        }
        storiesContainer.revalidate();
        storiesContainer.repaint();
        cardLayout.show(cardsPanel, "LIST");
    }

    private JPanel createStoryCard(StarStory story) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Constants.ACCENT_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel qLabel = new JLabel(story.getQuestion());
        qLabel.setFont(Constants.FONT_BODY_BOLD);
        qLabel.setForeground(Constants.TEXT_PRIMARY);
        textPanel.add(qLabel);
        textPanel.add(Box.createVerticalStrut(6));

        String preview = story.getSituation();
        if (preview != null && preview.length() > 80) {
            preview = preview.substring(0, 80) + "...";
        }
        JLabel pLabel = new JLabel(preview != null ? preview : "");
        pLabel.setFont(Constants.FONT_SMALL);
        pLabel.setForeground(Constants.TEXT_SECONDARY);
        textPanel.add(pLabel);

        card.add(textPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton editBtn = new RoundedButton("Edit");
        editBtn.setPreferredSize(new Dimension(80, 32));
        editBtn.addActionListener(e -> openEditor(story));
        btnPanel.add(editBtn);

        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    private void buildEditorPanel() {
        editorPanel = new JPanel(new BorderLayout());
        editorPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 36, 16, 36));

        JLabel title = new JLabel("STAR Story Editor");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        RoundedButton backBtn = new RoundedButton("← Back");
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> showList());
        header.add(backBtn, BorderLayout.EAST);

        editorPanel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 36, 28, 36));

        // Question
        JLabel qLbl = new JLabel("Interview Question:");
        qLbl.setFont(Constants.FONT_BODY_BOLD);
        qLbl.setForeground(Constants.TEXT_PRIMARY);
        form.add(qLbl);
        form.add(Box.createVerticalStrut(4));
        questionCombo = new JComboBox<>(COMMON_QUESTIONS);
        questionCombo.setEditable(true);
        questionCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(questionCombo);
        form.add(Box.createVerticalStrut(20));

        // STAR Fields
        situationArea = addTextArea(form, "Situation", "Set the scene and give the necessary details of your example.");
        taskArea = addTextArea(form, "Task", "Describe what your responsibility was in that situation.");
        actionArea = addTextArea(form, "Action", "Explain exactly what steps you took to address it.");
        resultArea = addTextArea(form, "Result", "Share what outcomes your actions achieved (use metrics if possible).");

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        RoundedButton delBtn = new RoundedButton("Delete");
        delBtn.setBackground(Constants.ACCENT_DANGER);
        delBtn.setPreferredSize(new Dimension(100, 40));
        delBtn.addActionListener(e -> deleteCurrentStory());
        bottomRow.add(delBtn);

        RoundedButton saveBtn = new RoundedButton("Save Story");
        saveBtn.setPreferredSize(new Dimension(120, 40));
        saveBtn.addActionListener(e -> saveCurrentStory());
        bottomRow.add(saveBtn);

        form.add(bottomRow);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        editorPanel.add(scroll, BorderLayout.CENTER);
    }

    private JTextArea addTextArea(JPanel parent, String label, String tooltip) {
        JLabel l = new JLabel(label + " - " + tooltip);
        l.setFont(Constants.FONT_BODY_BOLD);
        l.setForeground(Constants.TEXT_PRIMARY);
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));

        JTextArea area = new JTextArea(4, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Constants.FONT_BODY);
        area.setBackground(Constants.BG_INPUT);
        area.setForeground(Constants.TEXT_PRIMARY);
        area.setCaretColor(Constants.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.BORDER_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        parent.add(scroll);
        parent.add(Box.createVerticalStrut(16));
        return area;
    }

    private void openEditor(StarStory story) {
        currentStory = story != null ? story : new StarStory();
        
        if (story != null) {
            questionCombo.setSelectedItem(story.getQuestion());
            situationArea.setText(story.getSituation());
            taskArea.setText(story.getTask());
            actionArea.setText(story.getAction());
            resultArea.setText(story.getResult());
        } else {
            questionCombo.setSelectedIndex(0);
            situationArea.setText("");
            taskArea.setText("");
            actionArea.setText("");
            resultArea.setText("");
        }
        
        cardLayout.show(cardsPanel, "EDITOR");
    }

    private void saveCurrentStory() {
        User user = mainFrame.getCurrentUser();
        if (user == null) return;
        
        String q = questionCombo.getSelectedItem() != null ? questionCombo.getSelectedItem().toString() : "";
        if (q.trim().isEmpty()) {
            ToastNotification.error("Question cannot be empty.");
            return;
        }

        currentStory.setUserId(user.getId());
        currentStory.setQuestion(q);
        currentStory.setSituation(situationArea.getText().trim());
        currentStory.setTask(taskArea.getText().trim());
        currentStory.setAction(actionArea.getText().trim());
        currentStory.setResult(resultArea.getText().trim());

        starService.saveStory(currentStory);
        ToastNotification.success("STAR Story saved successfully!");
        showList();
    }

    private void deleteCurrentStory() {
        if (currentStory != null && currentStory.getId() > 0) {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Delete this story?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                starService.deleteStory(currentStory.getId(), mainFrame.getCurrentUser().getId());
                ToastNotification.success("Story deleted.");
                showList();
            }
        } else {
            showList(); // Just go back if it was new
        }
    }
}
