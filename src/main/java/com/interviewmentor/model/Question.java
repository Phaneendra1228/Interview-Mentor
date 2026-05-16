package com.interviewmentor.model;

/**
 * Represents a technical interview question.
 */
public class Question {
    private int id;
    private String category;
    private String difficulty; // Easy, Medium, Hard
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; // "A", "B", "C", or "D"
    private String explanation;
    private String tags;

    public Question() {}

    public Question(int id, String category, String difficulty, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctAnswer, String explanation, String tags) {
        this.id = id;
        this.category = category;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.tags = tags;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    /** Get option text by letter A-D */
    public String getOptionByLetter(String letter) {
        switch (letter.toUpperCase()) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default: return "";
        }
    }
}
