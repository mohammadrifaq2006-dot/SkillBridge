package com.skillbridge.lms;

import com.skillbridge.lms.config.SecurityConfig;
import com.skillbridge.lms.controller.AdminController;
import com.skillbridge.lms.controller.AuthController;
import com.skillbridge.lms.controller.CourseController;
import com.skillbridge.lms.dto.CourseResponse;
import com.skillbridge.lms.exception.GlobalExceptionHandler;
import com.skillbridge.lms.security.CustomUserDetailsService;
import com.skillbridge.lms.security.JwtAuthenticationEntryPoint;
import com.skillbridge.lms.security.JwtAuthenticationFilter;
import com.skillbridge.lms.security.JwtTokenProvider;
import com.skillbridge.lms.service.AuthService;
import com.skillbridge.lms.service.ContactService;
import com.skillbridge.lms.service.CourseService;
import com.skillbridge.lms.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CourseController.class, AdminController.class, AuthController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private AuthService authService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private ContactService contactService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void publicGetCourses_ShouldAllowUnauthenticatedAccess() throws Exception {
        when(courseService.getCourses(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightOptions_ShouldAllowRenderFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/courses")
                        .header("Origin", "https://skillbridge-frontend.onrender.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://skillbridge-frontend.onrender.com"));
    }

    @Test
    void createCourse_ShouldReturn401_WhenUnauthenticated() throws Exception {
        String courseJson = """
                {
                  "title": "Test Course",
                  "category": "Testing",
                  "difficulty": "Beginner",
                  "description": "Valid course description for testing.",
                  "instructor": "Prof. Test",
                  "duration": "4 Weeks",
                  "price": 0,
                  "syllabus": "Module 1: Unit Testing",
                  "prerequisites": "None",
                  "published": true
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.edu", roles = {"LEARNER"})
    void createCourse_ShouldReturn403_WhenAuthenticatedAsLearner() throws Exception {
        String courseJson = """
                {
                  "title": "Test Course",
                  "category": "Testing",
                  "difficulty": "Beginner",
                  "description": "Valid course description for testing.",
                  "instructor": "Prof. Test",
                  "duration": "4 Weeks",
                  "price": 0,
                  "syllabus": "Module 1: Unit Testing",
                  "prerequisites": "None",
                  "published": true
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@skillbridge.edu", roles = {"ADMIN"})
    void createCourse_ShouldReturn201_WhenAuthenticatedAsAdmin() throws Exception {
        CourseResponse created = new CourseResponse();
        created.setId(99L);
        created.setTitle("Test Course");
        created.setDifficulty("Beginner");
        created.setPrice(BigDecimal.ZERO);
        created.setFree(true);
        created.setPublished(true);

        when(courseService.createCourse(any())).thenReturn(created);

        String courseJson = """
                {
                  "title": "Test Course",
                  "category": "Testing",
                  "difficulty": "Beginner",
                  "description": "Valid course description for testing.",
                  "instructor": "Prof. Test",
                  "duration": "4 Weeks",
                  "price": 0,
                  "syllabus": "Module 1: Unit Testing",
                  "prerequisites": "None",
                  "published": true
                }
                """;

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Course"));
    }

    @Test
    @WithMockUser(username = "student@example.edu", roles = {"LEARNER"})
    void adminUsersEndpoint_ShouldReturn403_ForLearnerRole() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@skillbridge.edu", roles = {"ADMIN"})
    void adminCoursesEndpoint_ShouldReturn200_ForAdminRole() throws Exception {
        when(courseService.getAllCoursesForAdmin()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk());
    }

    @Test
    void registerEndpoint_ShouldReturn400WithValidationErrors_WhenInputIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "email": "not-an-email",
                  "password": "123",
                  "confirmPassword": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}
