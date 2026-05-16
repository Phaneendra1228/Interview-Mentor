package com.interviewmentor.model;

public class StarStory {
    private int id;
    private int userId;
    private String question;
    private String situation;
    private String task;
    private String action;
    private String result;

    public StarStory() {}

    public StarStory(int userId, String question, String situation, String task, String action, String result) {
        this.userId = userId;
        this.question = question;
        this.situation = situation;
        this.task = task;
        this.action = action;
        this.result = result;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
