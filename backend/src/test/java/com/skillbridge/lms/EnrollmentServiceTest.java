package com.skillbridge.lms;

import com.skillbridge.lms.dto.EnrollmentResponse;
import com.skillbridge.lms.dto.ProgressUpdateRequest;
import com.skillbridge.lms.entity.*;
import com.skillbridge.lms.exception.DuplicateResourceException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.ProgressRepository;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User learner;
    private User otherLearner;
    private Course course;

    @BeforeEach
    void setUp() {
        learner = new User("Priya Menon", "priya@example.edu", "hashed", Role.LEARNER);
        learner.setId(1L);

        otherLearner = new User("Rohan Das", "rohan@example.edu", "hashed", Role.LEARNER);
        otherLearner.setId(2L);

        course = new Course(
                "React Fundamentals",
                "Web Development",
                "Learn React basics",
                "Prof. Ananya",
                "4 Weeks",
                BigDecimal.ZERO,
                "Module 1\nModule 2",
                "HTML/JS",
                null
        );
        course.setId(10L);
    }

    @Test
    void enrollInCourse_ShouldCreateEnrollmentWithZeroProgress_WhenNotAlreadyEnrolled() {
        when(userRepository.findByEmailIgnoreCase("priya@example.edu")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        EnrollmentResponse response = enrollmentService.enrollInCourse("priya@example.edu", 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(0, response.getCompletionPercentage());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("React Fundamentals", response.getCourseTitle());
    }

    @Test
    void enrollInCourse_ShouldThrowDuplicateResourceException_WhenAlreadyEnrolled() {
        when(userRepository.findByEmailIgnoreCase("priya@example.edu")).thenReturn(Optional.of(learner));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> enrollmentService.enrollInCourse("priya@example.edu", 10L));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void updateProgress_ShouldMarkCompleted_WhenPercentageReaches100() {
        Enrollment enrollment = new Enrollment(learner, course);
        enrollment.setId(100L);
        Progress progress = new Progress(enrollment, 25);
        enrollment.setProgress(progress);

        when(userRepository.findByEmailIgnoreCase("priya@example.edu")).thenReturn(Optional.of(learner));
        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse updated = enrollmentService.updateProgress(
                "priya@example.edu",
                100L,
                new ProgressUpdateRequest(100)
        );

        assertEquals(100, updated.getCompletionPercentage());
        assertEquals("COMPLETED", updated.getStatus());
    }

    @Test
    void updateProgress_ShouldThrowAccessDenied_WhenAnotherLearnerAttemptsUpdate() {
        Enrollment enrollment = new Enrollment(learner, course);
        enrollment.setId(100L);

        when(userRepository.findByEmailIgnoreCase("rohan@example.edu")).thenReturn(Optional.of(otherLearner));
        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));

        assertThrows(AccessDeniedException.class, () ->
                enrollmentService.updateProgress("rohan@example.edu", 100L, new ProgressUpdateRequest(50))
        );
    }
}
