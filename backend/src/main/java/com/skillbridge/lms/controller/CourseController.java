package com.skillbridge.lms.controller;

import com.skillbridge.lms.dto.CourseRequest;
import com.skillbridge.lms.dto.CourseResponse;
import com.skillbridge.lms.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "Course catalog browsing and administrator CRUD endpoints")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Retrieve courses with optional keyword search and category filter")
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(courseService.getCourses(keyword, category));
    }

    @GetMapping("/categories")
    @Operation(summary = "Retrieve distinct course categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(courseService.getCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a single course by ID")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new course (Admin only)")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing course (Admin only)")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a course by ID (Admin only)")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully."));
    }
}
