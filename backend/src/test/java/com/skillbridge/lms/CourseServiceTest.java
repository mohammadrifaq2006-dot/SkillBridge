package com.skillbridge.lms;

import com.skillbridge.lms.dto.CourseRequest;
import com.skillbridge.lms.dto.CourseResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;
    private CourseRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course(
                "Spring Boot Microservices",
                "Backend Development",
                "Comprehensive Spring Boot and REST API development course.",
                "Dr. Rajesh Verma",
                "6 Weeks",
                BigDecimal.ZERO,
                "Module 1: Intro\nModule 2: JPA",
                "Core Java",
                null
        );
        sampleCourse.setId(1L);

        sampleRequest = new CourseRequest();
        sampleRequest.setTitle("Spring Boot Microservices");
        sampleRequest.setCategory("Backend Development");
        sampleRequest.setDescription("Comprehensive Spring Boot and REST API development course.");
        sampleRequest.setInstructor("Dr. Rajesh Verma");
        sampleRequest.setDuration("6 Weeks");
        sampleRequest.setPrice(BigDecimal.ZERO);
        sampleRequest.setSyllabus("Module 1: Intro\nModule 2: JPA");
        sampleRequest.setPrerequisites("Core Java");
    }

    @Test
    void getCourses_ShouldReturnFilteredCourses() {
        when(courseRepository.searchCourses("Spring", "Backend Development"))
                .thenReturn(List.of(sampleCourse));

        List<CourseResponse> results = courseService.getCourses("Spring", "Backend Development");

        assertEquals(1, results.size());
        assertEquals("Spring Boot Microservices", results.get(0).getTitle());
        assertTrue(results.get(0).isFree());
    }

    @Test
    void getCourseById_ShouldReturnCourse_WhenExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

        CourseResponse response = courseService.getCourseById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Dr. Rajesh Verma", response.getInstructor());
    }

    @Test
    void getCourseById_ShouldThrowResourceNotFound_WhenMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(99L));
    }

    @Test
    void createCourse_ShouldPersistAndReturnCourseResponse() {
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        CourseResponse created = courseService.createCourse(sampleRequest);

        assertEquals(5L, created.getId());
        assertEquals("Spring Boot Microservices", created.getTitle());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_ShouldModifyExistingCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        sampleRequest.setTitle("Updated Spring Boot Course");
        sampleRequest.setPrice(new BigDecimal("299.00"));

        CourseResponse updated = courseService.updateCourse(1L, sampleRequest);

        assertEquals("Updated Spring Boot Course", updated.getTitle());
        assertFalse(updated.isFree());
        assertEquals(new BigDecimal("299.00"), updated.getPrice());
    }

    @Test
    void deleteCourse_ShouldRemoveEnrollmentsAndCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

        courseService.deleteCourse(1L);

        verify(enrollmentRepository).deleteByCourseId(1L);
        verify(courseRepository).delete(sampleCourse);
    }
}
