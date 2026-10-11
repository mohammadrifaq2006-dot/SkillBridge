package com.skillbridge.lms.controller;

import com.skillbridge.lms.dto.ContactInquiryResponse;
import com.skillbridge.lms.dto.EnrollmentResponse;
import com.skillbridge.lms.dto.UserResponse;
import com.skillbridge.lms.service.AuthService;
import com.skillbridge.lms.service.ContactService;
import com.skillbridge.lms.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Administrator endpoints for viewing registered users, enrollments, and inquiries")
public class AdminController {

    private final AuthService authService;
    private final EnrollmentService enrollmentService;
    private final ContactService contactService;

    public AdminController(AuthService authService,
                           EnrollmentService enrollmentService,
                           ContactService contactService) {
        this.authService = authService;
        this.enrollmentService = enrollmentService;
        this.contactService = contactService;
    }

    @GetMapping("/users")
    @Operation(summary = "View all registered users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/enrollments")
    @Operation(summary = "View all course enrollments (Admin only)")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/inquiries")
    @Operation(summary = "View all submitted contact inquiries (Admin only)")
    public ResponseEntity<List<ContactInquiryResponse>> getAllInquiries() {
        return ResponseEntity.ok(contactService.getAllInquiries());
    }
}
