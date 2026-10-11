package com.skillbridge.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 1000, message = "Question must be under 1000 characters")
    private String message;

    private List<ChatMessageDto> history = new ArrayList<>();

    public ChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDto> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDto> history) {
        this.history = history;
    }
}
