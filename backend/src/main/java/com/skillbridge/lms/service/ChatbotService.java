package com.skillbridge.lms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.lms.dto.ChatMessageDto;
import com.skillbridge.lms.dto.ChatRequest;
import com.skillbridge.lms.dto.ChatResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.ApiException;
import com.skillbridge.lms.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${skillbridge.llm.api-key:}")
    private String apiKey;

    @Value("${skillbridge.llm.provider:gemini}")
    private String provider;

    @Value("${skillbridge.llm.model:gemini-2.5-flash}")
    private String model;

    @Value("${skillbridge.llm.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    public ChatbotService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", isApiKeyConfigured());
        status.put("provider", provider);
        status.put("model", model);
        status.put("indexedCourseCount", courseRepository.count());
        return status;
    }

    @Transactional(readOnly = true)
    public ChatResponse askQuestion(ChatRequest request) {
        List<Course> allCourses = courseRepository.findAll();
        List<Course> relevantCourses = retrieveRelevantCourses(request.getMessage(), allCourses);
        List<String> retrievedTitles = relevantCourses.stream()
                .map(Course::getTitle)
                .toList();

        if (!isApiKeyConfigured()) {
            String configMsg = "External LLM API key is not configured on the backend. " +
                    "To enable live AI chatbot responses, set the environment variable LLM_API_KEY (or GEMINI_API_KEY) " +
                    "before starting the Spring Boot server. No simulated or fake AI response was generated.";
            return new ChatResponse(configMsg, false, retrievedTitles);
        }

        String ragContext = buildCourseContextPrompt(relevantCourses, allCourses.size());
        String reply = callExternalLlmApi(ragContext, request.getHistory(), request.getMessage());
        return new ChatResponse(reply, true, retrievedTitles);
    }

    List<Course> retrieveRelevantCourses(String query, List<Course> allCourses) {
        if (allCourses.isEmpty()) {
            return List.of();
        }
        String normalized = query == null ? "" : query.toLowerCase();
        String[] tokens = normalized.split("[^a-z0-9]+");

        List<Course> matched = new ArrayList<>();
        for (Course course : allCourses) {
            String searchable = (course.getTitle() + " " + course.getCategory() + " " +
                    course.getDescription() + " " + course.getInstructor() + " " +
                    course.getSyllabus() + " " + course.getPrerequisites()).toLowerCase();

            boolean hit = false;
            for (String token : tokens) {
                if (token.length() >= 3 && searchable.contains(token)) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                matched.add(course);
            }
        }

        if (matched.isEmpty()) {
            return allCourses.stream().limit(5).toList();
        }
        return matched.stream().limit(5).toList();
    }

    private String buildCourseContextPrompt(List<Course> courses, int totalCourses) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the SkillBridge Learning Assistant for the SkillBridge Online Learning Platform.\n");
        sb.append("Answer student questions clearly, concisely, and accurately using the real MySQL course catalog data below.\n");
        sb.append("Total courses in MySQL catalog: ").append(totalCourses).append("\n\n");
        sb.append("Retrieved Relevant Courses from MySQL:\n");

        for (Course c : courses) {
            String priceLabel = (c.getPrice() == null || c.getPrice().compareTo(BigDecimal.ZERO) == 0)
                    ? "Free"
                    : "Paid (Rs. " + c.getPrice() + ")";
            sb.append("- Title: ").append(c.getTitle()).append("\n")
              .append("  Category: ").append(c.getCategory()).append("\n")
              .append("  Instructor: ").append(c.getInstructor()).append("\n")
              .append("  Duration: ").append(c.getDuration()).append(" | Pricing: ").append(priceLabel).append("\n")
              .append("  Prerequisites: ").append(c.getPrerequisites()).append("\n")
              .append("  Description: ").append(c.getDescription()).append("\n")
              .append("  Syllabus: ").append(c.getSyllabus().replace("\n", "; ")).append("\n\n");
        }
        sb.append("Platform features: Students can browse the course catalog, register/login, enroll in courses, update module completion progress on the Learner Dashboard, and download a completion certificate once a course reaches 100%.\n");
        return sb.toString();
    }

    private String callExternalLlmApi(String systemPrompt, List<ChatMessageDto> history, String userMessage) {
        try {
            if ("openai".equalsIgnoreCase(provider) || apiUrl.contains("chat/completions")) {
                return callOpenAiCompatibleApi(systemPrompt, history, userMessage);
            }
            return callGeminiApi(systemPrompt, history, userMessage);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to communicate with external LLM API", ex);
            throw new ApiException("Failed to reach external LLM API: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    private String callGeminiApi(String systemPrompt, List<ChatMessageDto> history, String userMessage) throws Exception {
        StringBuilder conversationText = new StringBuilder(systemPrompt).append("\nConversation:\n");
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                ChatMessageDto msg = history.get(i);
                conversationText.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }
        conversationText.append("User: ").append(userMessage).append("\nAssistant:");

        Map<String, Object> part = Map.of("text", conversationText.toString());
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> payload = Map.of("contents", List.of(content));

        String jsonBody = objectMapper.writeValueAsString(payload);
        String targetUrl = apiUrl.contains("?key=") ? apiUrl : apiUrl + "?key=" + apiKey.trim();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("External Gemini API returned status {}: {}", response.statusCode(), response.body());
            throw new ApiException("External LLM API returned status " + response.statusCode() + ". Please verify your LLM_API_KEY.", HttpStatus.BAD_GATEWAY);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                return parts.get(0).path("text").asText();
            }
        }
        throw new ApiException("Received an empty response from the external LLM API.", HttpStatus.BAD_GATEWAY);
    }

    private String callOpenAiCompatibleApi(String systemPrompt, List<ChatMessageDto> history, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                ChatMessageDto msg = history.get(i);
                String role = "assistant".equalsIgnoreCase(msg.getRole()) ? "assistant" : "user";
                messages.add(Map.of("role", role, "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(payload);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ApiException("External LLM API returned status " + response.statusCode() + ". Please verify your LLM_API_KEY.", HttpStatus.BAD_GATEWAY);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            return choices.get(0).path("message").path("content").asText();
        }
        throw new ApiException("Received an empty response from the external LLM API.", HttpStatus.BAD_GATEWAY);
    }
}
