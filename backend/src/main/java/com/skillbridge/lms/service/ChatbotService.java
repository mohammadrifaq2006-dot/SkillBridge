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
import java.net.http.HttpTimeoutException;
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

    @Value("${skillbridge.llm.api-url:}")
    private String apiUrl;

    public ChatbotService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public String resolveEffectiveApiUrl() {
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            return apiUrl.trim();
        }
        String effectiveModel = (model != null && !model.trim().isEmpty()) ? model.trim() : "gemini-2.5-flash";
        if ("openai".equalsIgnoreCase(provider)) {
            return "https://api.openai.com/v1/chat/completions";
        }
        return "https://generativelanguage.googleapis.com/v1beta/models/" + effectiveModel + ":generateContent";
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatus() {
        List<Course> publishedCourses = courseRepository.searchCourses(null, null);
        Map<String, Object> status = new HashMap<>();
        status.put("configured", isApiKeyConfigured());
        status.put("provider", provider != null ? provider : "gemini");
        status.put("model", model != null ? model : "gemini-2.5-flash");
        status.put("indexedCourseCount", publishedCourses.size());
        return status;
    }

    @Transactional(readOnly = true)
    public ChatResponse askQuestion(ChatRequest request) {
        List<Course> publishedCourses = courseRepository.searchCourses(null, null);
        List<Course> relevantCourses = retrieveRelevantCourses(request.getMessage(), publishedCourses);
        List<String> retrievedTitles = relevantCourses.stream()
                .map(Course::getTitle)
                .toList();

        if (!isApiKeyConfigured()) {
            String configMsg = "The AI Learning Assistant is currently unconfigured because the server environment variable " +
                    "LLM_API_KEY (or GEMINI_API_KEY) has not been set on the backend. " +
                    "Please configure a valid Gemini API key in your backend environment variables to enable live AI responses.";
            return new ChatResponse(configMsg, false, retrievedTitles);
        }

        String ragContext = buildCourseContextPrompt(relevantCourses, publishedCourses.size());
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
                    course.getDifficulty() + " " + course.getDescription() + " " +
                    course.getInstructor() + " " + course.getSyllabus() + " " +
                    course.getPrerequisites()).toLowerCase();

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
        sb.append("Total published courses in MySQL catalog: ").append(totalCourses).append("\n\n");

        if (courses.isEmpty()) {
            sb.append("No published courses are currently stored in the database.\n\n");
        } else {
            sb.append("Retrieved Relevant Courses from MySQL:\n");
            for (Course c : courses) {
                String priceLabel = (c.getPrice() == null || c.getPrice().compareTo(BigDecimal.ZERO) == 0)
                        ? "Free"
                        : "Paid (Rs. " + c.getPrice() + ")";
                sb.append("- Title: ").append(c.getTitle()).append("\n")
                  .append("  Category: ").append(c.getCategory())
                  .append(" | Difficulty: ").append(c.getDifficulty()).append("\n")
                  .append("  Instructor: ").append(c.getInstructor()).append("\n")
                  .append("  Duration: ").append(c.getDuration()).append(" | Pricing: ").append(priceLabel).append("\n")
                  .append("  Prerequisites: ").append(c.getPrerequisites()).append("\n")
                  .append("  Description: ").append(c.getDescription()).append("\n")
                  .append("  Syllabus: ").append(c.getSyllabus().replace("\n", "; ")).append("\n\n");
            }
        }
        sb.append("Platform features: Students can browse the course catalog, register/login, enroll in courses, update module completion progress on the Learner Dashboard, and download a completion certificate once a course reaches 100%.\n");
        return sb.toString();
    }

    private String callExternalLlmApi(String systemPrompt, List<ChatMessageDto> history, String userMessage) {
        String effectiveUrl = resolveEffectiveApiUrl();
        try {
            if ("openai".equalsIgnoreCase(provider) || effectiveUrl.contains("chat/completions")) {
                return callOpenAiCompatibleApi(effectiveUrl, systemPrompt, history, userMessage);
            }
            return callGeminiApi(effectiveUrl, systemPrompt, history, userMessage);
        } catch (ApiException ex) {
            throw ex;
        } catch (HttpTimeoutException ex) {
            log.warn("Timed out waiting for external LLM provider response");
            throw new ApiException("The AI provider timed out while generating a response. Please try again.", HttpStatus.GATEWAY_TIMEOUT);
        } catch (Exception ex) {
            log.error("Failed to communicate with external LLM provider: {}", ex.getClass().getSimpleName());
            throw new ApiException("The AI Learning Assistant is temporarily unavailable due to a provider connection error.", HttpStatus.BAD_GATEWAY);
        }
    }

    private String callGeminiApi(String baseEndpoint, String systemPrompt, List<ChatMessageDto> history, String userMessage) throws Exception {
        StringBuilder conversationText = new StringBuilder(systemPrompt).append("\nConversation:\n");
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                ChatMessageDto msg = history.get(i);
                if (msg != null && msg.getContent() != null) {
                    conversationText.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
                }
            }
        }
        conversationText.append("User: ").append(userMessage).append("\nAssistant:");

        Map<String, Object> part = Map.of("text", conversationText.toString());
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> payload = Map.of("contents", List.of(content));

        String jsonBody = objectMapper.writeValueAsString(payload);
        String targetUrl = baseEndpoint.contains("?key=") ? baseEndpoint : baseEndpoint + "?key=" + apiKey.trim();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode == 429) {
            throw new ApiException("The AI provider rate limit has been reached (HTTP 429). Please wait a moment and try again.", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (statusCode == 400 || statusCode == 401 || statusCode == 403) {
            log.warn("External Gemini API rejected request with status {}", statusCode);
            throw new ApiException("The AI provider rejected the request (HTTP " + statusCode + "). Please verify that LLM_API_KEY and LLM_MODEL are valid.", HttpStatus.BAD_GATEWAY);
        }
        if (statusCode < 200 || statusCode >= 300) {
            log.warn("External Gemini API returned unexpected status {}", statusCode);
            throw new ApiException("The AI provider returned an error (HTTP " + statusCode + "). Please try again later.", HttpStatus.BAD_GATEWAY);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                String text = parts.get(0).path("text").asText("");
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        throw new ApiException("The AI provider returned an empty or malformed response.", HttpStatus.BAD_GATEWAY);
    }

    private String callOpenAiCompatibleApi(String endpointUrl, String systemPrompt, List<ChatMessageDto> history, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                ChatMessageDto msg = history.get(i);
                if (msg != null && msg.getContent() != null) {
                    String role = "assistant".equalsIgnoreCase(msg.getRole()) ? "assistant" : "user";
                    messages.add(Map.of("role", role, "content", msg.getContent()));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(payload);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode == 429) {
            throw new ApiException("The AI provider rate limit has been reached (HTTP 429). Please wait a moment and try again.", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (statusCode == 401 || statusCode == 403) {
            throw new ApiException("The AI provider rejected the API key (HTTP " + statusCode + "). Please verify LLM_API_KEY.", HttpStatus.BAD_GATEWAY);
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new ApiException("The AI provider returned an error (HTTP " + statusCode + ").", HttpStatus.BAD_GATEWAY);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            String text = choices.get(0).path("message").path("content").asText("");
            if (!text.isBlank()) {
                return text;
            }
        }
        throw new ApiException("The AI provider returned an empty or malformed response.", HttpStatus.BAD_GATEWAY);
    }
}
