package com.skillbridge.lms.service;

import com.skillbridge.lms.dto.EnrollmentResponse;
import com.skillbridge.lms.dto.ProgressUpdateRequest;
import com.skillbridge.lms.entity.*;
import com.skillbridge.lms.exception.DuplicateResourceException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import com.skillbridge.lms.repository.ProgressRepository;
import com.skillbridge.lms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ProgressRepository progressRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public EnrollmentResponse enrollInCourse(String userEmail, Long courseId) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new DuplicateResourceException("You are already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment(user, course);
        Progress progress = new Progress(enrollment, 0);
        enrollment.setProgress(progress);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("User ID={} enrolled in course ID={} '{}'", user.getId(), course.getId(), course.getTitle());
        return EnrollmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return enrollmentRepository.findByUserIdOrderByEnrollmentDateDesc(user.getId())
                .stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateProgress(String userEmail, Long enrollmentId, ProgressUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (!enrollment.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only update progress for your own enrollments.");
        }

        int percentage = request.getCompletionPercentage();
        Progress progress = enrollment.getProgress();
        if (progress == null) {
            progress = new Progress(enrollment, percentage);
            enrollment.setProgress(progress);
        } else {
            progress.setCompletionPercentage(percentage);
            progress.setLastUpdated(LocalDateTime.now());
        }

        if (percentage >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        } else {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        }

        progressRepository.save(progress);
        Enrollment updated = enrollmentRepository.save(enrollment);
        log.info("Updated progress for enrollment ID={} user ID={} to {}%", enrollmentId, user.getId(), percentage);
        return EnrollmentResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }
}
