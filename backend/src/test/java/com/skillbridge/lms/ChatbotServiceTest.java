package com.skillbridge.lms;

import com.skillbridge.lms.dto.ChatRequest;
import com.skillbridge.lms.dto.ChatResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private Course reactCourse;
    private Course springCourse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatbotService, "apiKey", "");
        ReflectionTestUtils.setField(chatbotService, "provider", "gemini");
        ReflectionTestUtils.setField(chatbotService, "model", "gemini-2.5-flash");
        ReflectionTestUtils.setField(chatbotService, "apiUrl", "");

        reactCourse = new Course(
                "React & Modern Frontend Development",
                "Web Development",
                "Beginner",
                "Learn React and Vite.",
                "Prof. Ananya Sharma",
                "6 Weeks",
                BigDecimal.ZERO,
                "Module 1: JSX",
                "HTML/JS",
                null,
                true
        );
        springCourse = new Course(
                "Java Spring Boot & REST API Engineering",
                "Backend Development",
                "Intermediate",
                "Build Spring Boot REST APIs.",
                "Dr. Rajesh Verma",
                "8 Weeks",
                new BigDecimal("499.00"),
                "Module 1: Controllers",
                "Core Java",
                null,
                true
        );
    }

    @Test
    void getStatus_ShouldReturnUnconfiguredAndPublishedCourseCount() {
        when(courseRepository.searchCourses(null, null)).thenReturn(List.of(reactCourse, springCourse));

        Map<String, Object> status = chatbotService.getStatus();

        assertEquals(false, status.get("configured"));
        assertEquals(2, status.get("indexedCourseCount"));
        assertEquals("gemini-2.5-flash", status.get("model"));
    }

    @Test
    void askQuestion_ShouldRetrieveMatchingCoursesAndReturnConfigNotice_WhenApiKeyMissing() {
        when(courseRepository.searchCourses(null, null)).thenReturn(List.of(reactCourse, springCourse));

        ChatRequest request = new ChatRequest();
        request.setMessage("Which course teaches Spring Boot REST APIs?");

        ChatResponse response = chatbotService.askQuestion(request);

        assertFalse(response.isConfigured());
        assertTrue(response.getRetrievedCourses().contains("Java Spring Boot & REST API Engineering"));
        assertTrue(response.getReply().contains("LLM_API_KEY"));
    }

    @Test
    void resolveEffectiveApiUrl_ShouldBuildUrlFromConfiguredModel_WhenApiUrlIsBlank() {
        ReflectionTestUtils.setField(chatbotService, "model", "gemini-2.0-flash");
        String url = chatbotService.resolveEffectiveApiUrl();
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent", url);
    }
}
