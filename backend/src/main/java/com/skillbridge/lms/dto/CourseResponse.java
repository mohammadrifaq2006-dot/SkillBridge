package com.skillbridge.lms.dto;

import com.skillbridge.lms.entity.Course;

import java.math.BigDecimal;

public class CourseResponse {

    private Long id;
    private String title;
    private String category;
    private String description;
    private String instructor;
    private String duration;
    private BigDecimal price;
    private boolean free;
    private String syllabus;
    private String prerequisites;
    private String imageUrl;

    public CourseResponse() {
    }

    public static CourseResponse fromEntity(Course course) {
        CourseResponse dto = new CourseResponse();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setCategory(course.getCategory());
        dto.setDescription(course.getDescription());
        dto.setInstructor(course.getInstructor());
        dto.setDuration(course.getDuration());
        BigDecimal priceVal = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;
        dto.setPrice(priceVal);
        dto.setFree(priceVal.compareTo(BigDecimal.ZERO) == 0);
        dto.setSyllabus(course.getSyllabus());
        dto.setPrerequisites(course.getPrerequisites());
        dto.setImageUrl(course.getImageUrl());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
