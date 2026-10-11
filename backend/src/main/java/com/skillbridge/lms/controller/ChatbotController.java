package com.skillbridge.lms.controller;

import com.skillbridge.lms.dto.ChatRequest;
import com.skillbridge.lms.dto.ChatResponse;
import com.skillbridge.lms.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "AI Chatbot", description = "AI Learning Assistant with MySQL course context retrieval (RAG)")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/status")
    @Operation(summary = "Check whether external LLM API key is configured and view indexed course count")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(chatbotService.getStatus());
    }

    @PostMapping("/ask")
    @Operation(summary = "Send a question to the AI Chatbot with MySQL course RAG context")
    public ResponseEntity<ChatResponse> askQuestion(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatbotService.askQuestion(request));
    }
}
