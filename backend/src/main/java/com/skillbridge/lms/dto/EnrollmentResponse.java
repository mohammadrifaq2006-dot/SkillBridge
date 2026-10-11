package com.skillbridge.lms.dto;

import com.skillbridge.lms.entity.Course;
import com.skillbridge.lms.entity.Enrollment;
import com.skillbridge.lms.entity.Progress;
import com.skillbridge.lms.entity.User;

import java.time.LocalDateTime;

public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long courseId;
    private String courseTitle;
    private String courseCategory;
    private String courseInstructor;
    private String courseDuration;
    private String courseSyllabus;
    private LocalDateTime enrollmentDate;
    private String status;
    private Integer completionPercentage;
    private LocalDateTime lastUpdated;

    public EnrollmentResponse() {
    }

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        EnrollmentResponse dto = new EnrollmentResponse();
        dto.setId(enrollment.getId());

        User user = enrollment.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        }

        Course course = enrollment.getCourse();
        if (course != null) {
            dto.setCourseId(course.getId());
            dto.setCourseTitle(course.getTitle());
            dto.setCourseCategory(course.getCategory());
            dto.setCourseInstructor(course.getInstructor());
            dto.setCourseDuration(course.getDuration());
            dto.setCourseSyllabus(course.getSyllabus());
        }

        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setStatus(enrollment.getStatus() != null ? enrollment.getStatus().name() : "ACTIVE");

        Progress progress = enrollment.getProgress();
        if (progress != null) {
            dto.setCompletionPercentage(progress.getCompletionPercentage());
            dto.setLastUpdated(progress.getLastUpdated());
        } else {
            dto.setCompletionPercentage(0);
            dto.setLastUpdated(enrollment.getEnrollmentDate());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseCategory() {
        return courseCategory;
    }

    public void setCourseCategory(String courseCategory) {
        this.courseCategory = courseCategory;
    }

    public String getCourseInstructor() {
        return courseInstructor;
    }

    public void setCourseInstructor(String courseInstructor) {
        this.courseInstructor = courseInstructor;
    }

    public String getCourseDuration() {
        return courseDuration;
    }

    public void setCourseDuration(String courseDuration) {
        this.courseDuration = courseDuration;
    }

    public String getCourseSyllabus() {
        return courseSyllabus;
    }

    public void setCourseSyllabus(String courseSyllabus) {
        this.courseSyllabus = courseSyllabus;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
