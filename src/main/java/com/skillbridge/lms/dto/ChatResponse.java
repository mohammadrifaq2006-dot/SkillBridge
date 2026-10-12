package com.skillbridge.lms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatResponse {

    private String reply;
    private boolean configured;
    private List<String> retrievedCourses = new ArrayList<>();
    private LocalDateTime timestamp = LocalDateTime.now();

    public ChatResponse() {
    }

    public ChatResponse(String reply, boolean configured, List<String> retrievedCourses) {
        this.reply = reply;
        this.configured = configured;
        this.retrievedCourses = retrievedCourses != null ? retrievedCourses : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public List<String> getRetrievedCourses() {
        return retrievedCourses;
    }

    public void setRetrievedCourses(List<String> retrievedCourses) {
        this.retrievedCourses = retrievedCourses;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
