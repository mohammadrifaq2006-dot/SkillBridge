package com.skillbridge.lms.service;

import com.skillbridge.lms.dto.CourseRequest;
import com.skillbridge.lms.dto.CourseResponse;
import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CourseRepository;
import com.skillbridge.lms.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(String keyword, String category) {
        String trimmedKeyword = keyword != null ? keyword.trim() : null;
        String trimmedCategory = (category != null && !category.equalsIgnoreCase("ALL")) ? category.trim() : null;

        List<Course> courses = courseRepository.searchCourses(trimmedKeyword, trimmedCategory);
        return courses.stream()
                .map(CourseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCoursesForAdmin() {
        return courseRepository.findAllByOrderByIdAsc()
                .stream()
                .map(CourseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        return CourseResponse.fromEntity(course);
    }

    @Cacheable("courseCategories")
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return courseRepository.findDistinctCategories();
    }

    @CacheEvict(value = "courseCategories", allEntries = true)
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = new Course();
        applyRequestToEntity(request, course);
        Course saved = courseRepository.save(course);
        log.info("Created course ID={} title='{}' published={}", saved.getId(), saved.getTitle(), saved.getPublished());
        return CourseResponse.fromEntity(saved);
    }

    @CacheEvict(value = "courseCategories", allEntries = true)
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        applyRequestToEntity(request, course);
        Course updated = courseRepository.save(course);
        log.info("Updated course ID={} title='{}' published={}", updated.getId(), updated.getTitle(), updated.getPublished());
        return CourseResponse.fromEntity(updated);
    }

    @CacheEvict(value = "courseCategories", allEntries = true)
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        enrollmentRepository.deleteByCourseId(id);
        courseRepository.delete(course);
        log.info("Deleted course ID={} title='{}'", id, course.getTitle());
    }

    private void applyRequestToEntity(CourseRequest request, Course course) {
        course.setTitle(request.getTitle().trim());
        course.setCategory(request.getCategory().trim());
        course.setDifficulty(request.getDifficulty() != null && !request.getDifficulty().isBlank()
                ? request.getDifficulty().trim()
                : "Intermediate");
        course.setDescription(request.getDescription().trim());
        course.setInstructor(request.getInstructor().trim());
        course.setDuration(request.getDuration().trim());
        course.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        course.setSyllabus(request.getSyllabus().trim());
        course.setPrerequisites(request.getPrerequisites().trim());
        course.setImageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank()
                ? request.getImageUrl().trim()
                : null);
        course.setPublished(request.getPublished() == null || request.getPublished());
    }
}
