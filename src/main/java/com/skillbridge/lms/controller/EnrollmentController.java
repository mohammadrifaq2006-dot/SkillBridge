package com.skillbridge.lms.controller;

import com.skillbridge.lms.dto.EnrollmentResponse;
import com.skillbridge.lms.dto.ProgressUpdateRequest;
import com.skillbridge.lms.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments & Progress", description = "Course enrollment and learner progress tracking")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/course/{courseId}")
    @Operation(summary = "Enroll the authenticated user in a course")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        EnrollmentResponse enrollment = enrollmentService.enrollInCourse(authentication.getName(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @GetMapping("/my")
    @Operation(summary = "Retrieve enrollments and progress for the authenticated user")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(authentication.getName()));
    }

    @PutMapping("/{enrollmentId}/progress")
    @Operation(summary = "Update completion percentage for an enrollment")
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody ProgressUpdateRequest request,
            Authentication authentication) {
        EnrollmentResponse updated = enrollmentService.updateProgress(authentication.getName(), enrollmentId, request);
        return ResponseEntity.ok(updated);
    }
}
