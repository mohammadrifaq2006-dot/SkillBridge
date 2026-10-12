package com.skillbridge.lms.config;

import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Role;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${skillbridge.seed.sample-courses:true}")
    private boolean seedSampleCourses;

    @Value("${skillbridge.admin.email:}")
    private String adminEmail;

    @Value("${skillbridge.admin.password:}")
    private String adminPassword;

    @Value("${skillbridge.admin.name:SkillBridge Administrator}")
    private String adminName;

    public DataInitializer(CourseRepository courseRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedBootstrapAdminIfConfigured();
        backfillExistingCourseMetadata();
        if (seedSampleCourses) {
            seedCatalogCoursesIfEmpty();
        }
    }

    private void seedBootstrapAdminIfConfigured() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD environment variables not both set; skipping automatic admin provisioning.");
            return;
        }

        String normalizedEmail = adminEmail.toLowerCase().trim();
        Optional<User> existing = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isEmpty()) {
            User admin = new User(
                    (adminName != null && !adminName.isBlank()) ? adminName.trim() : "SkillBridge Administrator",
                    normalizedEmail,
                    passwordEncoder.encode(adminPassword),
                    Role.ADMIN
            );
            userRepository.save(admin);
            log.info("Provisioned configured administrator account: {}", normalizedEmail);
        } else {
            User admin = existing.get();
            boolean updated = false;
            if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                updated = true;
            }
            if (!passwordEncoder.matches(adminPassword, admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                updated = true;
            }
            if (updated) {
                userRepository.save(admin);
                log.info("Synchronized configured administrator account: {}", normalizedEmail);
            }
        }
    }

    private void backfillExistingCourseMetadata() {
        List<Course> existingCourses = courseRepository.findAll();
        boolean anyModified = false;
        for (Course course : existingCourses) {
            boolean modified = false;
            if (course.getPublished() == null) {
                course.setPublished(true);
                modified = true;
            }
            if (course.getDifficulty() == null || course.getDifficulty().isBlank()) {
                course.setDifficulty(inferDifficulty(course.getTitle()));
                modified = true;
            }
            if (modified) {
                courseRepository.save(course);
                anyModified = true;
            }
        }
        if (anyModified) {
            log.info("Backfilled difficulty/published defaults for existing courses.");
        }
    }

    private String inferDifficulty(String title) {
        if (title == null) return "Intermediate";
        String lower = title.toLowerCase();
        if (lower.contains("react") || lower.contains("testing") || lower.contains("docker")) {
            return "Beginner";
        }
        if (lower.contains("security") || lower.contains("ai") || lower.contains("rag")) {
            return "Advanced";
        }
        return "Intermediate";
    }

    private void seedCatalogCoursesIfEmpty() {
        if (courseRepository.count() > 0) {
            return;
        }

        List<Course> sampleCourses = List.of(
                new Course(
                        "React & Modern Frontend Development",
                        "Web Development",
                        "Beginner",
                        "Learn component-based frontend development with React, JSX, props, state management, React Router, forms, and REST API integration using Axios.",
                        "Prof. Ananya Sharma",
                        "6 Weeks",
                        BigDecimal.ZERO,
                        "Module 1: JSX, Components, and Props\nModule 2: State Management and useEffect Hook\nModule 3: Controlled Forms and Client-Side Validation\nModule 4: React Router and Protected Routes\nModule 5: Consuming REST APIs with Axios Interceptors",
                        "Basic HTML, CSS, and JavaScript fundamentals",
                        null,
                        true
                ),
                new Course(
                        "Java Spring Boot & REST API Engineering",
                        "Backend Development",
                        "Intermediate",
                        "Build production-ready Java backend services with Spring Boot, layered Controller-Service-Repository architecture, Jakarta Validation, and centralized exception handling.",
                        "Dr. Rajesh Verma",
                        "8 Weeks",
                        new BigDecimal("499.00"),
                        "Module 1: Spring Boot Fundamentals and Dependency Injection\nModule 2: Designing RESTful Controllers and DTOs\nModule 3: Request Validation with Jakarta Validation\nModule 4: Centralized Exception Handling (@ControllerAdvice)\nModule 5: Documenting APIs with Swagger / OpenAPI 3",
                        "Core Java and Object-Oriented Programming",
                        null,
                        true
                ),
                new Course(
                        "Relational Databases with MySQL & Spring Data JPA",
                        "Database Systems",
                        "Intermediate",
                        "Master relational schema design, primary and foreign keys, unique constraints, entity relationships, and persistence using Hibernate and Spring Data JPA.",
                        "Prof. Vikram Nair",
                        "5 Weeks",
                        BigDecimal.ZERO,
                        "Module 1: Relational Modeling, Keys, and Constraints in MySQL\nModule 2: Mapping Entities with JPA and Hibernate\nModule 3: One-to-Many and One-to-One Associations\nModule 4: Custom Queries and Spring Data Repositories\nModule 5: Transaction Management and Data Integrity",
                        "Basic understanding of SQL tables and queries",
                        null,
                        true
                ),
                new Course(
                        "Application Security with Spring Security & JWT",
                        "Cybersecurity",
                        "Advanced",
                        "Secure full-stack web applications using BCrypt password hashing, stateless JSON Web Tokens (JWT), authentication filters, and role-based access control.",
                        "Dr. Meera Krishnan",
                        "4 Weeks",
                        new BigDecimal("399.00"),
                        "Module 1: Password Hashing with BCrypt\nModule 2: Stateless Authentication and JWT Architecture\nModule 3: Implementing Spring Security Filter Chains\nModule 4: Role-Based Authorization (Learner vs Admin)\nModule 5: CORS Policies and Environment Variable Security",
                        "Java Spring Boot basics and HTTP headers",
                        null,
                        true
                ),
                new Course(
                        "Software Testing with JUnit 5 & Mockito",
                        "Quality Assurance",
                        "Beginner",
                        "Write unit and integration tests for backend services and controllers using JUnit 5 assertions, Mockito mocks, and Spring Security test utilities.",
                        "Prof. Siddharth Joshi",
                        "4 Weeks",
                        BigDecimal.ZERO,
                        "Module 1: Unit Testing Principles and JUnit 5 Lifecycle\nModule 2: Mocking Dependencies with Mockito\nModule 3: Testing Service Layer Business Logic\nModule 4: Testing Validation and Duplicate Constraints\nModule 5: Verifying Role-Based Endpoint Security",
                        "Basic Java programming",
                        null,
                        true
                ),
                new Course(
                        "AI Chatbot Integration & Practical RAG",
                        "Artificial Intelligence",
                        "Advanced",
                        "Integrate external Large Language Model (LLM) APIs into a Spring Boot backend and enrich prompts with real database records using Retrieval-Augmented Generation (RAG).",
                        "Dr. Karthik Iyer",
                        "5 Weeks",
                        new BigDecimal("599.00"),
                        "Module 1: Introduction to LLMs and Prompt Engineering\nModule 2: Calling External LLM APIs from Spring Boot\nModule 3: Securing API Keys via Environment Variables\nModule 4: Retrieving MySQL Context for RAG\nModule 5: Building a Conversational React Chat Interface",
                        "REST APIs and basic JSON structures",
                        null,
                        true
                ),
                new Course(
                        "Containerization & Deployment with Docker",
                        "DevOps & Cloud",
                        "Beginner",
                        "Package full-stack React, Spring Boot, and MySQL applications using multi-stage Dockerfiles, Docker Compose networking, and cloud deployment workflows.",
                        "Prof. Neha Gupta",
                        "3 Weeks",
                        BigDecimal.ZERO,
                        "Module 1: Docker Images, Containers, and Multi-Stage Builds\nModule 2: Writing Dockerfiles for Spring Boot and React\nModule 3: Orchestrating MySQL and App Services with Docker Compose\nModule 4: Environment Variables and Production Profiles\nModule 5: Cloud Deployment Checklist and Health Monitoring",
                        "Basic command-line familiarity",
                        null,
                        true
                )
        );

        courseRepository.saveAll(sampleCourses);
        log.info("Seeded {} sample catalog courses into MySQL (configurable via SEED_SAMPLE_COURSES).", sampleCourses.size());
    }
}
