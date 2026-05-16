package com.interviewmentor.model;

import java.time.LocalDateTime;

/**
 * Represents a Voice/Text log entry for a quiz session.
 * Maps to flowchart: "Voice/Text Logs", "Regit Voice Capture",
 * "Record & Process Response".
 */
public class SessionLog {
    private int id;
    private int sessionId;
    private int questionId;
    private String logType;         // "VOICE", "TEXT", "SYSTEM"
    private String content;         // Transcription or text response
    private String responseData;    // JSON metadata (confidence, duration, etc.)
    private double confidenceScore; // STT confidence 0.0-1.0
    private LocalDateTime createdAt;

    public SessionLog() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getResponseData() { return responseData; }
    public void setResponseData(String responseData) { this.responseData = responseData; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
